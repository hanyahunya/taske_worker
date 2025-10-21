package com.hanyahunya.worker.adapter.out.grpc;

import com.hanyahunya.grpc.ExecutionDetailsRequest;
import com.hanyahunya.grpc.ExecutionDetailsResponse;
import com.hanyahunya.grpc.TaskExecutionServiceGrpc;
import com.hanyahunya.worker.application.port.out.LoadExecutionDetailsPort;
import com.hanyahunya.worker.application.response.TaskDetailsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskGrpcAdapter implements LoadExecutionDetailsPort {

    private final TaskExecutionServiceGrpc.TaskExecutionServiceBlockingStub taskExecutionStub;

    @Override
    public TaskDetailsResponse getExecutionDetails(Long taskId) {
        ExecutionDetailsRequest request = ExecutionDetailsRequest.newBuilder()
                .setTaskId(taskId)
                .build();
        ExecutionDetailsResponse response;
        try {
             response = taskExecutionStub.getExecutionDetails(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Execution details from task: " + e.getMessage(), e);
        }
        return TaskDetailsResponse.fromProto(response);
    }
}
