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
        // 4b. [신규] param_schema를 순회하며 #_ (고정값) 및 %_ (환경변수) 처리
        Map<String, Object> paramSchema = capabilityInfo.paramSchema();

        if (paramSchema != null && paramSchema.containsKey("properties")) {
            Map<String, Object> properties = (Map<String, Object>) paramSchema.get("properties");

            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String schemaKey = entry.getKey(); // 예: "#_pageNo", "%_authKey"
                Map<String, Object> propDetails = (Map<String, Object>) entry.getValue();

                String finalKey;
                Object finalValue;

                if (schemaKey.startsWith("#_")) {
                    // 고정값: #_key -> key, value는 schema의 "value"
                    finalKey = schemaKey.substring(2); // 예: "pageNo"
                    finalValue = propDetails.get("value"); // 예: "1"

                } else if (schemaKey.startsWith("%_")) {
                    // 환경변수: %_key -> key, value는 schema의 "value"에 %_를 붙임
                    finalKey = schemaKey.substring(2); // 예: "authKey"
                    Object schemaValue = propDetails.get("value"); // 예: "apihub.kma.go.kr.secret"
                    // 예: "%_apihub.kma.go.kr.secret"
                    finalValue = (schemaValue != null) ? "%_" + schemaValue.toString() : null;

                } else {
                    // 일반 변수 (interpolatedConfig에서 이미 처리됨)
                    continue;
                }

                // 값이 null이 아닌 경우, *원본 스키마 키*를 기준으로 분배
                // (예: querySchema에 "#_pageNo"가 있는지 확인)
                // (      -> queryParams 에는 "pageNo": "1" 로 저장)
                if (finalValue != null) {

                    boolean inQuery = querySchema.contains(schemaKey);
                    boolean inBody = bodySchema.contains(schemaKey);
                    boolean inHeader = headerSchema.contains(schemaKey);

                    if (inQuery) {
                        queryParams.put(finalKey, finalValue);
                    } else if (inBody) {
                        body.put(finalKey, finalValue);
                    } else if (inHeader) {
                        headers.put(finalKey, String.valueOf(finalValue));
                    }
                }
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