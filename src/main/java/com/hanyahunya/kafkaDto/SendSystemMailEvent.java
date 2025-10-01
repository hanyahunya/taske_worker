package com.hanyahunya.kafkaDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SendSystemMailEvent {
    String to;
    String subject;
    String templateName;
    Map<String, String> variables;
    String locale;
}
