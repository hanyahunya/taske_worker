package com.hanyahunya.worker.application.command;

import lombok.Builder;

import java.util.Map;

@Builder
public record SendMailCommand(
   String to,
   String subject,
   String templateName,
   Map<String, String> variables,
   String locale
) {}
