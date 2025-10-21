package com.hanyahunya.worker.application.service.system;

import com.hanyahunya.worker.application.port.in.SystemActionExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SystemExecutorFactory {
    private final Map<String, SystemActionExecutor> systemActionExecutors;

    public SystemExecutorFactory(List<SystemActionExecutor> systemActionExecutors) {
        this.systemActionExecutors = systemActionExecutors.stream()
                .collect(Collectors.toMap(
                        SystemActionExecutor::getTopic,
                        adapter -> adapter
                ));
    }
    public SystemActionExecutor getAdapter(String topic) {
        SystemActionExecutor adapter = systemActionExecutors.get(topic);
        if (adapter == null) {
            throw new IllegalArgumentException("unsupported provider type: " + topic);
        }
        return adapter;
    }
}
