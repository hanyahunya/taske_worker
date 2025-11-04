package com.hanyahunya.worker.application.service;

import com.hanyahunya.worker.application.command.ExecuteTaskCommand;
import com.hanyahunya.worker.application.command.HttpExecutionCommand;
import com.hanyahunya.worker.application.port.in.ExecuteTaskUseCase;
import com.hanyahunya.worker.application.port.in.SystemActionExecutor;
import com.hanyahunya.worker.application.port.out.LoadExecutionDetailsPort;
// import com.hanyahunya.worker.application.port.out.IntegrationServicePort; // (향후 gRPC Port)
// import com.hanyahunya.worker.application.port.out.KafkaProducerPort; // (향후 Kafka Port)
import com.hanyahunya.worker.application.response.TaskDetailsResponse;
import com.hanyahunya.worker.application.service.helper.HttpExecutionCommandBuilder;
import com.hanyahunya.worker.application.service.system.SystemExecutorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set; // [!] Set 임포트 추가
import com.hanyahunya.worker.application.response.TaskDetailsResponse.ActionDetail;


@Slf4j
@Service
@RequiredArgsConstructor
public class ExecuteTaskService implements ExecuteTaskUseCase {

    private final LoadExecutionDetailsPort loadExecutionDetailsPort;
    private final HttpExecutionCommandBuilder httpExecutionCommandBuilder;
    private final VariableInterpolator variableInterpolator;
    private final SystemExecutorFactory systemExecutorFactory;

    // (향후 주입 예정)
    // private final IntegrationServicePort integrationServicePort;
    // private final KafkaProducerPort kafkaProducerPort;

    @Override
    public void executeTask(ExecuteTaskCommand command) {

        StopWatch totalTaskStopWatch = new StopWatch();
        totalTaskStopWatch.start();

        // worker -> task (gRPC)
        TaskDetailsResponse executionDetails = loadExecutionDetailsPort.getExecutionDetails(command.taskId());

        // output_schema에 정의된거 저장하는곳
        Map<String, Object> executionContext = new HashMap<>();

        log.debug("Initializing execution context with trigger data: {}", command.triggerOutput());
        command.triggerOutput().forEach((key, value) -> executionContext.put("trigger.output." + key, value));

        List<ActionDetail> actions = executionDetails.actionDetails();
        log.info("Task {} execution started. Processing {} actions.", command.taskId(), actions.size());


        Set<Integer> syncActionOrders = variableInterpolator.findReferencedActionOrders(actions);
        log.debug("동기 실행이 필요한 Action Orders: {}", syncActionOrders);


        Map<String, Object> result = null;

        for (ActionDetail action : actions) {

            // *** 개별 Action 시간 측정 시작 ***
            StopWatch actionStopWatch = new StopWatch();
            actionStopWatch.start();

            log.info("[Action {}] Executing capability: {}",
                    action.executionOrder(),
                    action.capabilityInfo().capabilityId());

            // #_%actionN.output.변수%_# -> 변수
            log.debug("[Action {}] Original Config: {}", action.executionOrder(), action.actionConfig());
            Map<String, Object> interpolatedConfig = variableInterpolator.interpolateMap(action.actionConfig(), executionContext);
            log.debug("[Action {}] Interpolated Config: {}", action.executionOrder(), interpolatedConfig);

            interpolatedConfig.putAll(action.capabilityInfo().paramSchema());
            log.debug("[Action {}] Final Config (User + Defaults): {}", action.executionOrder(), interpolatedConfig);
            log.debug("[Action {}] Final Config (User + Defaults): {}", action.executionOrder(), interpolatedConfig);
            log.debug("[Action {}] Final Config (User + Defaults): {}", action.executionOrder(), interpolatedConfig);

            // http나 내부에서 사용될 데이터 생성하는거
            String moduleId = action.capabilityInfo().moduleInfo().moduleId();
            HttpExecutionCommand httpData = httpExecutionCommandBuilder.build(action, interpolatedConfig);

            boolean needsSync = syncActionOrders.contains(action.executionOrder());
            log.info("-----------------기존 needSync: {}----------------", needsSync);
            log.info("httpData: {}", httpData);
            log.info("-----------------기존 needSync: {}----------------", needsSync);
            log.info("httpData: {}", httpData);

            // 7. 모듈 타입(SYSTEM / CUSTOM)에 따라 분기
            if ("SYSTEM".equals(moduleId)) {
                // 7a. SYSTEM 모듈 실행
                log.debug("[Action {}] Module type: SYSTEM (Topic: {})", action.executionOrder(), httpData.topic());
                SystemActionExecutor systemActionExecutor = systemExecutorFactory.getAdapter(httpData.topic());

                // [!] SYSTEM 모듈도 동기/비동기 실행을 구분

                if (needsSync) {
                    // [!] 7a-1. (동기) 이 액션의 output이 나중에 필요함 -> 동기 실행
                    log.info("[Action {}] Dependency found. Executing synchronously (Local Adapter).", action.executionOrder());
                    result = systemActionExecutor.execute(httpData);
                    log.debug("[Action {}] Execution result: {}", action.executionOrder(), result);
                } else {
                    // [!] 7a-2. (비동기) 이 액션의 output이 나중에 필요 없음
                    log.info("[Action {}] No dependency found. Executing (SYSTEM, async pattern).", action.executionOrder());

                    // (향후 SYSTEM 모듈도 Kafka 발행 등으로 비동기 처리 가능)
                    // 현재는 SystemActionExecutor가 동기식 인터페이스만 제공하므로,
                    // 동기식으로 실행하되, 그 결과를 컨텍스트에 저장하지 않음.
                    systemActionExecutor.execute(httpData);
                    log.warn("[Action {}] SYSTEM module execution (ASYNC) ran synchronously, result discarded.", action.executionOrder());

                    // 비동기 실행은 context에 저장할 즉각적인 결과가 없음
                    result = Map.of();
                }

            } else {
                // 7b. CUSTOM 모듈 실행 (gRPC 또는 Kafka)
                log.debug("[Action {}] Module type: CUSTOM ({})", action.executionOrder(), moduleId);
                log.debug("[Action {}] Built ExecutionData: {}", action.executionOrder(), httpData);

                if (needsSync) {
                    log.info("[Action {}] Dependency found. Executing synchronously (gRPC).", action.executionOrder());

                    // todo: integrationServicePort.execute(httpData) 등 gRPC Port 호출
                    log.warn("[Action {}] CUSTOM module execution (SYNC) is not yet implemented.", action.executionOrder());
                    result = Map.of("success", false, "error", "CUSTOM module execution not implemented");

                } else {
                    // [!] 8b. (비동기) 이 액션의 output이 나중에 필요 없음 -> Kafka 실행
                    log.info("[Action {}] No dependency found. Executing asynchronously (Kafka).", action.executionOrder());

                    // todo: kafkaProducerPort.publishAction(httpData) 등 Kafka Port 호출
                    log.warn("[Action {}] CUSTOM module execution (ASYNC) is not yet implemented.", action.executionOrder());

                    // 비동기 실행은 context에 저장할 즉각적인 결과가 없음
                    result = Map.of();
                }
                // ----------------------------------------------------------
            }

            // 9. 실행 결과(result)가 있다면 executionContext에 저장
            if (result != null && !result.isEmpty()) {
                String actionKey = "action" + action.executionOrder() + ".output.";
                result.forEach((key, value) -> executionContext.put(actionKey + key, value));
                log.debug("[Action {}] Saved result to executionContext: {}", action.executionOrder(), result);
            } else {
                log.debug("[Action {}] No result returned from execution.", action.executionOrder());
            }

            // *** 개별 Action 시간 측정 종료 ***
            actionStopWatch.stop();
            log.info("[Action {}] Finished capability: {} in {}ms",
                    action.executionOrder(),
                    action.capabilityInfo().capabilityId(),
                    actionStopWatch.getTotalTimeMillis());
        }

        // *** Task 전체 시간 측정 종료 ***
        totalTaskStopWatch.stop();
        log.info("Task {} execution finished in {}ms.", command.taskId(), totalTaskStopWatch.getTotalTimeMillis());

        log.debug("Final Execution Context: {}", executionContext);
    }
}