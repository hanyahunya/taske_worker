package com.hanyahunya.worker.application.port.in;

import com.hanyahunya.worker.application.command.HttpExecutionCommand;
import java.util.Map;

/**
 * "SYSTEM" 모듈의 하위 액션을 실행하기 위한 공통 인터페이스 (Strategy Pattern)
 */
public interface SystemActionExecutor {

    /**
     * 이 Executor가 처리할 topic 이름을 반환합니다.
     * (예: "system-mail-events")
     * @return Topic 이름
     */
    String getTopic();

    /**
     * 실제 액션 로직을 실행합니다.
     * @return 실행 결과 (executionContext에 저장될 Map)
     */
    Map<String, Object> execute(HttpExecutionCommand command);
}