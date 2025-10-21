package com.hanyahunya.worker.adapter.in.kafka;

import com.hanyahunya.kafkaDto.SendSystemMailEvent;
import com.hanyahunya.worker.application.command.SendSystemMailCommand;
import com.hanyahunya.worker.application.port.in.MailUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailEventKafkaAdapter {
    private final MailUseCase mailUseCase;

    @KafkaListener(topics = "system-mail-events", groupId = "worker-group")
    public void sendSystemMail(SendSystemMailEvent event){
        SendSystemMailCommand command = SendSystemMailCommand.builder()
                .to(event.getTo())
                .subject(event.getSubject())
                .templateName(event.getTemplateName())
                .locale(event.getLocale())
                .variables(event.getVariables())
                .build();
        mailUseCase.sendSystemMail(command);
    }
}
