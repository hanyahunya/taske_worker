package com.hanyahunya.worker.application.port.out;

import com.hanyahunya.worker.application.response.TaskDetailsResponse;

public interface LoadExecutionDetailsPort {
    TaskDetailsResponse getExecutionDetails(Long taskId);
}
