package com.weyland.prototype.bishopprototype.controller;

import com.weyland.starter.synthetichumancorestarter.command.Command;
import com.weyland.starter.synthetichumancorestarter.command.CommandExecutor;
import com.weyland.starter.synthetichumancorestarter.metrics.AndroidMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/commands")
public class CommandController {

    private final CommandExecutor commandExecutor;
    private final AndroidMetricsService metricsService;

    @Autowired
    public CommandController(CommandExecutor commandExecutor, AndroidMetricsService metricsService) {
        this.commandExecutor = commandExecutor;
        this.metricsService = metricsService;
    }

    @PostMapping
    public void submitCommand(@RequestBody Command command) {
        commandExecutor.submitCommand(command);
    }

    @GetMapping("/queue")
    public int getQueueSize() {
        return commandExecutor.getQueueSize();
    }

    @GetMapping("/stats/authors")
    public Map<String, Integer> getAuthorStats() {
        return metricsService.getAuthorCompleted();
    }
}