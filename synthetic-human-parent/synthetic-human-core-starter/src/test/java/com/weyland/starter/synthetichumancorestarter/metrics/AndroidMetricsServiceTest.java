package com.weyland.starter.synthetichumancorestarter.metrics;

import com.weyland.starter.synthetichumancorestarter.command.CommandExecutor;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AndroidMetricsServiceTest {
    @Test
    void testIncrementAuthorCompleted() {
        AndroidMetricsService service = new AndroidMetricsService(
                new SimpleMeterRegistry(),
                new CommandExecutor(10, null)
        );
        service.incrementAuthorCompleted("Ripley");
        service.incrementAuthorCompleted("Ripley");
        assertEquals(2, service.getAuthorCompleted().get("Ripley"));
    }
}