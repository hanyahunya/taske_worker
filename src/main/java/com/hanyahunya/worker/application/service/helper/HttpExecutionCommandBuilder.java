package com.hanyahunya.worker.application.service.helper;

import com.hanyahunya.worker.application.command.HttpExecutionCommand;
import com.hanyahunya.worker.application.response.TaskDetailsResponse;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HttpExecutionCommandBuilder {
    /**
     * ActionDetail과 치환된 Config를 기반으로 HttpExecutionData를 구축합니다.
     */
    @SuppressWarnings("unchecked")
    public HttpExecutionCommand build(TaskDetailsResponse.ActionDetail action, Map<String, Object> interpolatedConfig) {

        // 1. 필요한 정보 객체 추출
        var capabilityInfo = action.capabilityInfo();
        var moduleInfo = capabilityInfo.moduleInfo();
        var spec = capabilityInfo.executionSpec(); // Map<String, Object>

        // 2. 단순 정보 추출
        String method = (String) spec.get("method");
        Map<String, Object> outputSchema = capabilityInfo.outputSchema();
        String authType = moduleInfo.authType();
        String topic = (String) spec.get("topic");

        // 3. URL 조합
        String baseUrl = moduleInfo.apiBaseUrl();
        String endpoint = (String) spec.get("endpoint");
        String url = (baseUrl != null ? baseUrl : "") + (endpoint != null ? endpoint : "");

        // 4. Config를 Query, Body, Header로 분배
        Map<String, Object> queryParams = new HashMap<>();
        Map<String, Object> body = new HashMap<>();
        Map<String, String> headers = new HashMap<>();

        // spec에서 스키마를 List로 가져옵니다. (없으면 빈 리스트)
        List<String> querySchema = convertToList(spec.getOrDefault("querySchema", List.of()));
        List<String> bodySchema = convertToList(spec.getOrDefault("bodySchema", List.of()));
        List<String> headerSchema = convertToList(spec.getOrDefault("headerSchema", List.of()));

        // 치환된 설정값(interpolatedConfig)을 순회하며 각 스키마에 맞게 분배
        for (Map.Entry<String, Object> entry : interpolatedConfig.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (querySchema.contains(key)) {
                queryParams.put(key, value);
            } else if (bodySchema.contains(key)) {
                body.put(key, value);
            } else if (headerSchema.contains(key)) {
                headers.put(key, String.valueOf(value));
            } else {
                // 스키마에 정의되지 않은 값은 무시
            }
        }

        // 5. 최종 레코드 생성
        return new HttpExecutionCommand(url, method, queryParams, body, headers, outputSchema, authType, topic);
    }

    /**
     * Object를 List<String>으로 안전하게 변환합니다.
     */
    @SuppressWarnings("unchecked")
    private List<String> convertToList(Object obj) {
        if (obj instanceof List) {
            // 모든 항목이 String이라고 가정하고 스트리밍하여 변환
            return ((List<?>) obj).stream()
                    .map(String::valueOf)
                    .toList();
        }
        return List.of(); // 리스트가 아니면 빈 리스트 반환
    }
}