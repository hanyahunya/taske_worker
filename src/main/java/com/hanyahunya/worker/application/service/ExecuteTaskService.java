package com.hanyahunya.worker.application.service;

import com.hanyahunya.worker.application.command.ExecuteTaskCommand;
import com.hanyahunya.worker.application.command.HttpExecutionCommand;
import com.hanyahunya.worker.application.port.in.ExecuteTaskUseCase;
import com.hanyahunya.worker.application.port.in.SystemActionExecutor;
import com.hanyahunya.worker.application.port.out.LoadExecutionDetailsPort;
import com.hanyahunya.worker.application.response.TaskDetailsResponse;
import com.hanyahunya.worker.application.service.helper.HttpExecutionCommandBuilder;
import com.hanyahunya.worker.application.service.system.SystemExecutorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch; // (*** 1. StopWatch 임포트 ***)

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.hanyahunya.worker.application.response.TaskDetailsResponse.ActionDetail;


@Slf4j
@Service
@RequiredArgsConstructor
public class ExecuteTaskService implements ExecuteTaskUseCase {

    private final LoadExecutionDetailsPort loadExecutionDetailsPort;
    private final HttpExecutionCommandBuilder httpExecutionCommandBuilder;
    private final VariableInterpolator variableInterpolator;
    private final SystemExecutorFactory systemExecutorFactory;


    @Override
    public void executeTask(ExecuteTaskCommand command) {

        // (*** 2. Task 전체 시간 측정 시작 ***)
        StopWatch totalTaskStopWatch = new StopWatch();
        totalTaskStopWatch.start();

        TaskDetailsResponse executionDetails = loadExecutionDetailsPort.getExecutionDetails(command.taskId());

        Map<String, Object> executionContext = new HashMap<>();
        // (시뮬레이션 데이터)
        executionContext.put("action0.output.messageId", "테스트에용@@@@@@@@");
        executionContext.put("action0.output.success", "@@@@@@@@");

        log.debug("Initializing execution context with trigger data: {}", command.triggerOutput());
        command.triggerOutput().forEach((key, value) -> executionContext.put("trigger.output." + key, value));

        List<ActionDetail> actions = executionDetails.actionDetails();

        log.info("Task {} execution started. Processing {} actions.", command.taskId(), actions.size());

        Map<String, Object> result = null;

        for (ActionDetail action : actions) {

            // (*** 3. 개별 Action 시간 측정 시작 ***)
            StopWatch actionStopWatch = new StopWatch();
            actionStopWatch.start();

            log.info("[Action {}] Executing capability: {}",
                    action.executionOrder(),
                    action.capabilityInfo().capabilityId());

            // ... (변수 치환 로직) ...
            log.debug("[Action {}] Original Config: {}", action.executionOrder(), action.actionConfig());
            Map<String, Object> interpolatedConfig = variableInterpolator.interpolateMap(action.actionConfig(), executionContext);
            log.debug("[Action {}] Interpolated Config: {}", action.executionOrder(), interpolatedConfig);

            String moduleId = action.capabilityInfo().moduleInfo().moduleId();
            HttpExecutionCommand httpData = httpExecutionCommandBuilder.build(action, interpolatedConfig);

            if ("SYSTEM".equals(moduleId)) {
                log.debug("[Action {}] Module type: SYSTEM (Topic: {})", action.executionOrder(), httpData.topic());
                log.debug("[Action {}] Built ExecutionData: {}", action.executionOrder(), httpData);

                SystemActionExecutor systemActionExecutor = systemExecutorFactory.getAdapter(httpData.topic());
                result = systemActionExecutor.execute(httpData);

                log.debug("[Action {}] Execution result: {}", action.executionOrder(), result);

            } else {
                log.debug("[Action {}] Module type: CUSTOM ({})", action.executionOrder(), moduleId);
                log.debug("[Action {}] Built ExecutionData: {}", action.executionOrder(), httpData);

                // todo gRPC port 이용해서 응답 불러오기
                log.warn("[Action {}] CUSTOM module execution is not yet implemented.", action.executionOrder());
                result = Map.of("success", false, "error", "CUSTOM module execution not implemented");
            }

            // ... (결과 저장 로직) ...
            if (result != null && !result.isEmpty()) {
                String actionKey = "action" + action.executionOrder() + ".output.";
                result.forEach((key, value) -> executionContext.put(actionKey + key, value));
                log.debug("[Action {}] Saved result to executionContext: {}", action.executionOrder(), result);
            } else {
                log.debug("[Action {}] No result returned from execution.", action.executionOrder());
            }

            // (*** 4. 개별 Action 시간 측정 종료 및 로깅 ***)
            actionStopWatch.stop();
            log.info("[Action {}] Finished capability: {} in {}ms",
                    action.executionOrder(),
                    action.capabilityInfo().capabilityId(),
                    actionStopWatch.getTotalTimeMillis());
        }

        // (*** 5. Task 전체 시간 측정 종료 및 로깅 ***)
        totalTaskStopWatch.stop();
        log.info("Task {} execution finished in {}ms.", command.taskId(), totalTaskStopWatch.getTotalTimeMillis());

        log.debug("Final Execution Context: {}", executionContext);
    }
}