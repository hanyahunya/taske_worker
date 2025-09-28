package com.hanyahunya.worker.adapter.out;

import com.hanyahunya.worker.application.port.out.MailSenderPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailSenderAdapter implements MailSenderPort {

    private final JavaMailSender javaMailSender;

    @Override
    public void send(String to, String subject, String body) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setFrom(String.valueOf(new InternetAddress("noreply@hanyahunya.com", "Taske")));
            helper.setSubject(subject);
            helper.setText(body, true);
            javaMailSender.send(mimeMessage);
            // todo 여기부터 밑에 수정 필요
            log.info("Successfully sent email via SMTP to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email via SMTP to {}", to, e);
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
