package com.hanyahunya.worker.application.command;

import java.util.Map;

public record HttpExecutionCommand(
        String url,
        String method,
        Map<String, Object> queryParams,
        Map<String, Object> body,
        Map<String, String> headers,
        Map<String, Object> outputSchema,
        String authType,
        String topic
) {
}
