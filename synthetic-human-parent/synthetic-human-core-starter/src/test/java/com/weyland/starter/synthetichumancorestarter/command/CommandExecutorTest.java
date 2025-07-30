package com.weyland.starter.synthetichumancorestarter.command;

import com.weyland.starter.synthetichumancorestarter.metrics.AndroidMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class CommandExecutorTest {

    private CommandExecutor executor;

    @BeforeEach
    void setup() {
        AndroidMetricsService metricsService = Mockito.mock(AndroidMetricsService.class);
        // maxQueueSize = 2 для переполнения
        executor = new CommandExecutor(2, metricsService);
    }

    @Test
    void testCriticalCommandExecutesImmediately() {
        Command cmd = new Command("Check reactor", CommandPriority.CRITICAL, "Ripley", "2122-06-03T12:00:00Z");
        executor.submitCommand(cmd);
        assertEquals(0, executor.getQueueSize());
    }

    @Test
    void testCommonCommandGoesToQueue() {
        Command cmd = new Command("Clean floor", CommandPriority.COMMON, "Ash", "2122-06-03T13:00:00Z");
        executor.submitCommand(cmd);
        assertEquals(1, executor.getQueueSize());
    }

    @Test
    void testValidationFailsOnBadCommand() {
        Command badCmd = new Command("", CommandPriority.CRITICAL, "", "not-a-date");
        assertThrows(CommandValidationException.class, () -> executor.submitCommand(badCmd));
    }
}