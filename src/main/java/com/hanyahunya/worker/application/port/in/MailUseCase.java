package com.hanyahunya.worker.application.port.in;

import com.hanyahunya.worker.application.command.SendMailCommand;

public interface MailUseCase {
    void sendSystemMail(SendMailCommand command);
}
