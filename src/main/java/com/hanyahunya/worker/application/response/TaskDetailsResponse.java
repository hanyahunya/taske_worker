package com.hanyahunya.worker.application.response;

import com.hanyahunya.grpc.ActionInfo;
import com.hanyahunya.grpc.ExecutionDetailsResponse;
import com.hanyahunya.grpc.ModuleCapabilityInfo;
import com.hanyahunya.grpc.ModuleInfo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hanyahunya.worker.adapter.out.grpc.util.GrpcStructConverter.toMap;

/**
 * gRPC 응답을 변환한, 애플리케이션 코어가 사용하는 순수 Java DTO (Record 버전)
 */
public record TaskDetailsResponse(
        List<ActionDetail> actionDetails
) {
    public record ActionDetail(
            int executionOrder,
            String capabilityId,
            Map<String, Object> actionConfig,
            CapabilityDetail capabilityInfo
    ) {}

    public record CapabilityDetail(
            String capabilityId,
            String executionType,
            Map<String, Object> executionSpec,
            Map<String, Object> outputSchema,
            ModuleDetail moduleInfo
    ) {}

    public record ModuleDetail(
            String moduleId,
            String authType,
            String apiBaseUrl
    ) {}


    // --- static 팩토리 메서드 (Proto -> Record 변환) ---

    /**
     * gRPC 응답 객체(GetExecutionDetailsResponse)를
     * 순수 Java DTO(TaskDetailsResponse)로 변환합니다.
     *
     * @param grpcResponse gRPC 서비스로부터 받은 응답
     * @return 애플리케이션 계층에서 사용할 DTO
     */
    public static TaskDetailsResponse fromProto(ExecutionDetailsResponse grpcResponse) {
        if (grpcResponse == null) {
            return new TaskDetailsResponse(List.of());
        }

        List<ActionDetail> actions = grpcResponse.getActionsList().stream()
                .map(TaskDetailsResponse::convertToActionDetail)
                .collect(Collectors.toList());

        return new TaskDetailsResponse(actions);
    }


    // --- private static 헬퍼 메서드 ---
    /**
     * gRPC ActionInfo -> ActionDetail 레코드
     */
    private static ActionDetail convertToActionDetail(ActionInfo actionInfo) {
        return new ActionDetail(
                actionInfo.getExecutionOrder(),
                actionInfo.getCapabilityId(),
                toMap(actionInfo.getActionConfig()), // Struct -> Map
                convertToCapabilityDetail(actionInfo.getCapabilityInfo())
        );
    }

    /**
     * gRPC ModuleCapabilityInfo -> CapabilityDetail 레코드
     */
    private static CapabilityDetail convertToCapabilityDetail(ModuleCapabilityInfo capabilityInfo) {
        return new CapabilityDetail(
                capabilityInfo.getCapabilityId(),
                capabilityInfo.getExecutionType(),
                toMap(capabilityInfo.getExecutionSpec()), // Struct -> Map
                toMap(capabilityInfo.getOutputSchema()),  // Struct -> Map
                convertToModuleDetail(capabilityInfo.getModuleInfo())
        );
    }

    /**
     * gRPC ModuleInfo -> ModuleDetail 레코드
     */
    private static ModuleDetail convertToModuleDetail(ModuleInfo moduleInfo) {
        return new ModuleDetail(
                moduleInfo.getModuleId(),
                moduleInfo.getAuthType(),
                moduleInfo.getApiBaseUrl()
        );
    }
}