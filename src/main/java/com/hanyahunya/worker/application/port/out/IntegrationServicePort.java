package com.hanyahunya.worker.application.port.out;

import com.hanyahunya.worker.application.command.HttpExecutionCommand;

import java.util.Map;

/**
 * 'GOOGLE' 등 외부 모듈 액션을 'integration-service'에 gRPC로 요청하는 포트
 */
public interface IntegrationServicePort {
    Map<String, Object> execute(HttpExecutionCommand command);
}