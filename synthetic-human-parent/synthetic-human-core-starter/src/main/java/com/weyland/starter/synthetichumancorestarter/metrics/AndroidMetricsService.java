package com.weyland.starter.synthetichumancorestarter.metrics;

import com.weyland.starter.synthetichumancorestarter.command.CommandExecutor;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class AndroidMetricsService {

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, AtomicInteger> authorCompleted = new ConcurrentHashMap<>();

    public AndroidMetricsService(MeterRegistry meterRegistry, CommandExecutor commandExecutor) {
        this.meterRegistry = meterRegistry;

        meterRegistry.gauge("android.busy.tasks", commandExecutor, CommandExecutor::getQueueSize);
        meterRegistry.gauge("android.queue.maxsize", commandExecutor, CommandExecutor::getMaxQueueSize);
        meterRegistry.gauge("android.completed.commands", commandExecutor, CommandExecutor::getExecutedCommands);
    }

    public void incrementAuthorCompleted(String author) {
        authorCompleted.computeIfAbsent(author, k -> {
            AtomicInteger gauge = new AtomicInteger(0);
            meterRegistry.gauge("android.completed.by.author",
                    java.util.Collections.singletonList(io.micrometer.core.instrument.Tag.of("author", author)), gauge);
            return gauge;
        }).incrementAndGet();
    }

    public Map<String, Integer> getAuthorCompleted() {
        return authorCompleted.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().get()
                ));
    }
}