package com.hanyahunya.worker.infra.config;

import com.hanyahunya.grpc.TaskExecutionServiceGrpc;
import io.grpc.ManagedChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcClientConfig {

    @Bean
    TaskExecutionServiceGrpc.TaskExecutionServiceBlockingStub taskExecutionServiceStub(GrpcChannelFactory channelFactory) {
        ManagedChannel channel = channelFactory.createChannel("task-service");
        return TaskExecutionServiceGrpc.newBlockingStub(channel);
    }

}
