package com.hanyahunya.worker.application.port.out;

import java.util.Locale;
import java.util.Map;

public interface TemplateRenderPort {
    String render(String templateName, Map<String, String> variables, Locale locale);
}
