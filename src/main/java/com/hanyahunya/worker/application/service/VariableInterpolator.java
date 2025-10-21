package com.hanyahunya.worker.application.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class VariableInterpolator {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("#_%(.+?)%_#");

    /**
     * Map의 값들을 재귀적으로 탐색하며 변수를 치환합니다.
     * (이 메서드가 이 서비스의 메인 진입점입니다.)
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> interpolateMap(Map<String, Object> map, Map<String, Object> context) {
        Map<String, Object> newMap = new HashMap<>(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            newMap.put(entry.getKey(), interpolate(entry.getValue(), context));
        }
        return newMap;
    }

    /**
     * List의 항목들을 재귀적으로 탐색하며 변수를 치환
     */
    private List<Object> interpolateList(List<?> list, Map<String, Object> context) {
        List<Object> newList = new ArrayList<>(list.size());
        for (Object item : list) {
            newList.add(interpolate(item, context));
        }
        return newList;
    }

    /**
     * 객체의 타입에 따라 적절한 치환 메소드를 호출
     */
    @SuppressWarnings("unchecked")
    private Object interpolate(Object value, Map<String, Object> context) {
        if (value instanceof String) {
            return interpolateString((String) value, context);
        }
        if (value instanceof List) {
            return interpolateList((List<?>) value, context);
        }
        if (value instanceof Map) {
            // actionConfig 내부의 Map은 Map<String, Object>라고 가정
            return interpolateMap((Map<String, Object>) value, context);
        }
        // String, List, Map이 아닌 (숫자, 불리언 등) 타입은 그대로 반환
        return value;
    }

    /**
     * 문자열 내의 #_%...%_# 변수들을 executionContext의 값으로 치환합니다.
     */
    private String interpolateString(String template, Map<String, Object> context) {
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1); // "action0.output.messageId" 같은 변수명
            String replacement;

            if (context.containsKey(key)) {
                replacement = String.valueOf(context.get(key));
            } else {
                // context에 키가 없으면, 디버깅을 위해 원본 변수 태그를 그대로 둡니다.
                replacement = matcher.group(0);
                // (선택) 경고 로그 출력
                // log.warn("Execution context missing variable: {}", replacement);
            }

            // 치환될 문자열에 $ 또는 \가 포함되어 있을 경우를 대비해 quoteReplacement 사용
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}