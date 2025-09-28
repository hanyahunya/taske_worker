package com.hanyahunya.worker.infra.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
public class MessageSourceConfig {

    // Spring Boot의 기본 MessageSource Bean을 새로운 Bean으로 대체
    @Bean
    public MessageSource messageSource() throws IOException {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");

        // 클래스패스에서 i18n 폴더 아래의 모든 .properties 파일을 읽음
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:i18n/**/*.properties");

        // 파일 경로에서 basename(대표 이름)만 추출
        Set<String> basenames = new HashSet<>();
        Pattern pattern = Pattern.compile("i18n/(.+?)_..\\.properties"); // i18n/auth/verification_ko.properties -> verification

        for (Resource resource : resources) {
            String uri = resource.getURI().toString();
            Matcher matcher = pattern.matcher(uri);
            if (matcher.find()) {
                // classpath:/i18n/auth 형태로 추출
                basenames.add("classpath:/i18n/" + matcher.group(1));
            }
        }

        // 찾은 basename들을 MessageSource에 등록
        messageSource.setBasenames(basenames.toArray(new String[0]));
        return messageSource;
    }
}