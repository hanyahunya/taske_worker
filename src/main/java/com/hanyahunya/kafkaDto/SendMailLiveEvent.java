package com.hanyahunya.kafkaDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SendMailLiveEvent {
    String to;
    String subject;
    String templateName;
    Map<String, String> variables;
    String locale;
}
