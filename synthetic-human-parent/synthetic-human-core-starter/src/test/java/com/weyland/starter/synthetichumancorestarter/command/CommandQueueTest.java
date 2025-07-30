package com.weyland.starter.synthetichumancorestarter.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandQueueTest {

    @Test
    void testAddAndPoll() {
        CommandQueue queue = new CommandQueue(1);
        Command cmd = new Command("a", CommandPriority.COMMON, "Ash", "2122-06-03T13:00:00Z");
        queue.add(cmd);
        assertEquals(1, queue.size());
        assertEquals(cmd, queue.poll());
        assertEquals(0, queue.size());
    }

    @Test
    void testOverflow() {
        CommandQueue queue = new CommandQueue(1);
        queue.add(new Command("a", CommandPriority.COMMON, "Ash", "2122-06-03T13:00:00Z"));
        assertThrows(IllegalStateException.class, () ->
                queue.add(new Command("b", CommandPriority.COMMON, "Ash", "2122-06-03T13:00:00Z")));
    }
}