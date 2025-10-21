package com.hanyahunya.worker.application.command;

import java.util.Map;
import java.util.UUID;

public record ExecuteTaskCommand(
        UUID userId,
        Long taskId,
        Map<String, Object> triggerOutput
) {}