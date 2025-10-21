package com.hanyahunya.worker.adapter.out;

import com.hanyahunya.worker.application.port.out.MailSenderPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailSenderAdapter implements MailSenderPort {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void send(List<String> to, String subject, String body) {

        if (to == null || to.isEmpty()) {
            log.warn("Recipient list is empty. Skipping email send for subject: {}", subject);
            return;
        }

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            // to do 추후 smtp 서버를 운용하면 각 다른 요청으로 보낼수있게 변경
            helper.setTo(from);

            helper.setBcc(to.toArray(new String[0]));

            helper.setFrom(String.valueOf(new InternetAddress(from, "Taske")));
            helper.setSubject(subject);
            helper.setText(body, true);

            javaMailSender.send(mimeMessage);

            log.info("Successfully sent email via Bcc to {} recipients. Subject: {}", to.size(), subject);
        } catch (MessagingException e) {
            log.error("Failed to send email via Bcc to {} recipients. Subject: {}", to.size(), subject, e);
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            log.error("Failed to encode sender name", e);
            throw new RuntimeException(e);
        }
    }
}