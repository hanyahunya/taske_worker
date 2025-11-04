package com.hanyahunya.worker.application.service;

import com.hanyahunya.worker.application.response.TaskDetailsResponse; // [!] import
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet; // [!] import
import java.util.List;
import java.util.Map;
import java.util.Set; // [!] import
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class VariableInterpolator {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("#_%(.+?)%_#");

    // #_%action(숫자).output.(모든것)%_# 형식을 찾고 (숫자) 부분만 그룹 1로 추출
    private static final Pattern ACTION_OUTPUT_SCAN_PATTERN = Pattern.compile("#_%action(\\d+)\\.output\\..+?%_#");

    /**
     * [수정] Task의 모든 액션을 미리 스캔하여,
     * 다른 액션에 의해 output이 참조되는 액션의 실행 순서(executionOrder) Set을 반환합니다.
     */
    public Set<Integer> findReferencedActionOrders(List<TaskDetailsResponse.ActionDetail> allActions) {

        // 1. 결과 Set을 준비
        Set<Integer> referencedOrders = new HashSet<>();

        // 2. 모든 액션의 config를 순회하며 재귀적으로 스캔
        for (TaskDetailsResponse.ActionDetail action : allActions) {
            // 3. 재귀 스캔 메소드 호출
            scanForReferencedOrders(action.actionConfig(), referencedOrders);
        }

        // 4. 참조된 order 목록 반환 (예: {0, 2})
        return referencedOrders;
    }

    /**
     * [수정] actionConfig(Map, List, String)을 재귀적으로 탐색하여
     * 발견된 action output (예: #_%action0.output...%_#)의
     * executionOrder(예: 0)를 Set에 직접 추가합니다.
     *
     * @param configValue 스캔할 객체 (String, Map, List 등)
     * @param referencedOrders (결과를 누적할 Set)
     */
    @SuppressWarnings("unchecked")
    private void scanForReferencedOrders(Object configValue, Set<Integer> referencedOrders) {
        if (configValue instanceof String) {
            Matcher matcher = ACTION_OUTPUT_SCAN_PATTERN.matcher((String) configValue);
            while (matcher.find()) {
                try {
                    referencedOrders.add(Integer.parseInt(matcher.group(1)));
                } catch (NumberFormatException ignored) {}
            }
        } else if (configValue instanceof Map) {
            for (Object value : ((Map<String, Object>) configValue).values()) {
                scanForReferencedOrders(value, referencedOrders);
            }
        } else if (configValue instanceof List) {
            for (Object item : (List<?>) configValue) {
                scanForReferencedOrders(item, referencedOrders);
            }
        }
    }



    @SuppressWarnings("unchecked")
    public Map<String, Object> interpolateMap(Map<String, Object> map, Map<String, Object> context) {
        Map<String, Object> newMap = new HashMap<>(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            newMap.put(entry.getKey(), interpolate(entry.getValue(), context));
        }
        return newMap;
    }

    private List<Object> interpolateList(List<?> list, Map<String, Object> context) {
        List<Object> newList = new ArrayList<>(list.size());
        for (Object item : list) {
            newList.add(interpolate(item, context));
        }
        return newList;
    }

    @SuppressWarnings("unchecked")
    private Object interpolate(Object value, Map<String, Object> context) {
        if (value instanceof String) {
            return interpolateString((String) value, context);
        }
        if (value instanceof List) {
            return interpolateList((List<?>) value, context);
        }
        if (value instanceof Map) {
            return interpolateMap((Map<String, Object>) value, context);
        }
        return value;
    }

    private String interpolateString(String template, Map<String, Object> context) {
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement;

            if (context.containsKey(key)) {
                replacement = String.valueOf(context.get(key));
            } else {
                replacement = matcher.group(0);
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}