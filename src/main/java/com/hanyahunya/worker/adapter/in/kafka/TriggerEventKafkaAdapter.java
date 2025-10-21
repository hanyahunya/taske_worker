package com.hanyahunya.worker.adapter.in.kafka;

import com.hanyahunya.kafkaDto.TriggerFiredEvent; //
import com.hanyahunya.worker.application.command.ExecuteTaskCommand;
import com.hanyahunya.worker.application.port.in.ExecuteTaskUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TriggerEventKafkaAdapter {

    private final ExecuteTaskUseCase executeTaskUseCase;

    @KafkaListener(topics = "trigger-fired-events", groupId = "worker-group")
    public void handleTriggerFired(TriggerFiredEvent event) {
        log.info("TriggerFiredEvent 수신. Task ID: {}", event.taskId());

        try {
            ExecuteTaskCommand command = new ExecuteTaskCommand(
                    event.userId(),
                    event.taskId(),
                    event.triggerOutput()
            );
            executeTaskUseCase.executeTask(command);

        } catch (Exception e) {
            log.error("Task ID {} 실행 오케스트레이션 실패", event.taskId(), e);
            // TODO: DLQ 발행 등 오류 처리
        }
    }
}