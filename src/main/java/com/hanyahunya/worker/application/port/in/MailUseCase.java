package com.hanyahunya.worker.application.port.in;

import com.hanyahunya.worker.application.command.SendMailCommand;
import com.hanyahunya.worker.application.command.SendSystemMailCommand;

public interface MailUseCase {
    void sendSystemMail(SendSystemMailCommand command);
    void sendMail(SendMailCommand command);
}
