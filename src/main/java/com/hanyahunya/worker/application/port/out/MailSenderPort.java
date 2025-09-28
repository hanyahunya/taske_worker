package com.hanyahunya.worker.application.port.out;

public interface MailSenderPort {
    void send(String to, String subject, String body);
}
