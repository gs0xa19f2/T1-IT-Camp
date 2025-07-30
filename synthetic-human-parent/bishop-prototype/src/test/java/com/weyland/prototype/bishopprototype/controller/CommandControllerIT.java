package com.weyland.prototype.bishopprototype.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weyland.starter.synthetichumancorestarter.command.Command;
import com.weyland.starter.synthetichumancorestarter.command.CommandPriority;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CommandControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSubmitValidCommand() throws Exception {
        Command cmd = new Command(
                "Check engines", CommandPriority.CRITICAL, "Ripley", "2122-06-03T10:00:00Z"
        );

        mockMvc.perform(post("/api/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk());
    }

    @Test
    void testSubmitInvalidCommandReturnsBadRequest() throws Exception {
        Command cmd = new Command(
                "", CommandPriority.CRITICAL, "", "bad-date"
        );
        mockMvc.perform(post("/api/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Command validation error"));
    }
}