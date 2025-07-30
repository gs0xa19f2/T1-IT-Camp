package com.weyland.prototype.bishopprototype.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weyland.starter.synthetichumancorestarter.command.Command;
import com.weyland.starter.synthetichumancorestarter.command.CommandPriority;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "synthetichuman.queue.max-size=1")
class CommandControllerQueueOverflowIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testQueueOverflowReturnsTooManyRequests() throws Exception {
        Command cmd1 = new Command("Task 1", CommandPriority.COMMON, "Ash", "2122-06-03T13:00:00Z");
        Command cmd2 = new Command("Task 2", CommandPriority.COMMON, "Ash", "2122-06-03T13:00:00Z");

        mockMvc.perform(post("/api/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd2)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("Queue overflow"));
    }
}