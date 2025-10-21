package com.hanyahunya.worker.application.command;

import java.util.List;

public record SendMailCommand(
        List<String> to,
        String subject,
        String html
) {
}
