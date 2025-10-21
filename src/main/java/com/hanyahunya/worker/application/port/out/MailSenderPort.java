package com.hanyahunya.worker.application.port.out;

import java.util.List;

public interface MailSenderPort {
    void send(List<String> to, String subject, String body);
}
