package com.hanyahunya.worker.adapter.out.grpc.util;

import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Protobuf의 Struct/Value 타입을 Java의 Map/List/Object로 변환하는 유틸리티 클래스
 */
public final class GrpcStructConverter {

    // private 생성자로 인스턴스화 방지
    private GrpcStructConverter() {}

    /**
     * Protobuf Struct를 Java의 Map<String, Object>로 변환
     */
    public static Map<String, Object> toMap(Struct struct) {
        if (struct == null) {
            return new HashMap<>();
        }
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Value> entry : struct.getFieldsMap().entrySet()) {
            map.put(entry.getKey(), fromValue(entry.getValue()));
        }
        return map;
    }

    /**
     * Protobuf Value를 Java Object로 재귀적으로 변환
     */
    public static Object fromValue(Value value) {
        return switch (value.getKindCase()) {
            case NULL_VALUE -> null;
            case NUMBER_VALUE -> value.getNumberValue();
            case STRING_VALUE -> value.getStringValue();
            case BOOL_VALUE -> value.getBoolValue();
            case STRUCT_VALUE -> toMap(value.getStructValue());
            case LIST_VALUE -> toList(value.getListValue());
            default -> null; // KIND_NOT_SET
        };
    }

    /**
     * Protobuf ListValue를 Java List<Object>로 변환
     */
    public static List<Object> toList(ListValue listValue) {
        if (listValue == null) {
            return new ArrayList<>();
        }
        return listValue.getValuesList().stream()
                .map(GrpcStructConverter::fromValue) // 재귀 호출
                .collect(Collectors.toList());
    }
}