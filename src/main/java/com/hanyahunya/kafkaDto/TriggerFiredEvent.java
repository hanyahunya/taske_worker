package com.hanyahunya.kafkaDto;

import java.util.Map;
import java.util.UUID;

public record TriggerFiredEvent(
        UUID userId,
        Long taskId,
        /**
         * Trigger가 실행되면서 생성된 초기 데이터 (컨텍스트의 시작점).
         * 첫 번째 Action은 이 데이터를 {{trigger.output...}} 형태로 참조가능
         * capabilityId 로 gRPC등 으로 task 서비스에서 불러와서 output_schema 읽어온후 아래 output과 키가 일치하는지 확인후 실행
         * {"videoId": "xyz-123", "title": "새로운 강의", ...} 이런식
         */
        Map<String, Object> triggerOutput
) {}
