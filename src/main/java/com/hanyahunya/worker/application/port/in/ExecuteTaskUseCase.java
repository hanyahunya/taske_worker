package com.hanyahunya.worker.application.port.in;

import com.hanyahunya.worker.application.command.ExecuteTaskCommand;

public interface ExecuteTaskUseCase {
    void executeTask(ExecuteTaskCommand command);
}