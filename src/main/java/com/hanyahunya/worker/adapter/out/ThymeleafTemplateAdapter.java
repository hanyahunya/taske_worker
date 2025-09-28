package com.hanyahunya.worker.adapter.out;

import com.hanyahunya.worker.application.port.out.TemplateRenderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ThymeleafTemplateAdapter implements TemplateRenderPort {

    private final TemplateEngine templateEngine;

    @Override
    public String render(String templateName, Map<String, String> variables, Locale locale) {
        // 방식 바꿀수도.
        String viewName = templateName.replace('-', '/');

        Context context = new Context(locale);
        context.setVariables(Map.copyOf(variables));
        return templateEngine.process(viewName, context);
    }
}
