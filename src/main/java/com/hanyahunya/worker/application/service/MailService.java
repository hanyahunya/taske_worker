package com.hanyahunya.worker.application.service;

import com.hanyahunya.worker.application.command.SendMailCommand;
import com.hanyahunya.worker.application.port.in.MailUseCase;
import com.hanyahunya.worker.application.port.out.MailSenderPort;
import com.hanyahunya.worker.application.port.out.TemplateRenderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MailService implements MailUseCase {

    private final TemplateRenderPort templateRenderPort;
    private final MailSenderPort mailSenderPort;
    private final MessageSource messageSource;
    private static final Set<String> SUPPORTED_LOCALES = Set.of("ja-JP", "ko-KR");

    @Override
    public void sendSystemMail(SendMailCommand command) {
        Locale userLocale = Locale.getDefault();

        // 2. 입력된 로케일이 지원 목록에 있는지 확인
        if (command.locale() != null && SUPPORTED_LOCALES.contains(command.locale())) {
            userLocale = Locale.forLanguageTag(command.locale());
        }

        // 템플릿 렌더링 포트 사용
        String emailBody = templateRenderPort.render(command.templateName(), command.variables(), userLocale);

        // MessageSource에서 제목(subject) 텍스트를 가져옴
        String subject = messageSource.getMessage(command.subject(), null, userLocale);

        mailSenderPort.send(command.to(), subject, emailBody);
    }
}
