package com.hanyahunya.worker.application.service.system;

import com.hanyahunya.worker.application.command.HttpExecutionCommand;
import com.hanyahunya.worker.application.command.SendMailCommand;
import com.hanyahunya.worker.application.port.in.MailUseCase;
import com.hanyahunya.worker.application.port.in.SystemActionExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SystemMailExecutor implements SystemActionExecutor {
    private final MailUseCase mailUseCase;

    @Override
    public String getTopic() {
        return "system-mail-events";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(HttpExecutionCommand command) {
        Map<String, Object> body = command.body();
        String subject = (String) body.get("subject");
        String htmlBody = (String) body.get("htmlBody");
        List<String> to = (List<String>) body.get("to");

        SendMailCommand mailCommand = new SendMailCommand(to, subject, htmlBody);
        mailUseCase.sendMail(mailCommand);
        return Map.of();
    }
}
