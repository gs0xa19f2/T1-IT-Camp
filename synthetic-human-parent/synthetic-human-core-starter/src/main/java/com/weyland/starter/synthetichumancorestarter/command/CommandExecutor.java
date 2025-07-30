package com.weyland.starter.synthetichumancorestarter.command;

import com.weyland.starter.synthetichumancorestarter.audit.WeylandWatchingYou;
import com.weyland.starter.synthetichumancorestarter.metrics.AndroidMetricsService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class CommandExecutor {
    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);
    private final CommandQueue queue;
    private final Validator validator;
    private final AndroidMetricsService metricsService;

    public CommandExecutor(
            @Value("${synthetichuman.queue.max-size:100}") int maxQueueSize,
            AndroidMetricsService metricsService
    ) {
        this.queue = new CommandQueue(maxQueueSize);
        this.metricsService = metricsService;
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this::processQueue);
    }

    public void submitCommand(Command command) {
        validateCommand(command);

        if (command.getPriority() == CommandPriority.CRITICAL) {
            execute(command);
        } else {
            queue.add(command);
        }
    }

    private void validateCommand(Command command) {
        Set<ConstraintViolation<Command>> violations = validator.validate(command);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<Command> violation : violations) {
                sb.append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("; ");
            }
            throw new CommandValidationException(sb.toString());
        }
    }

    private void processQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Command command = queue.take();
                execute(command);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @WeylandWatchingYou
    public void execute(Command command) {
        logger.info("Executing command: {}", command);
        queue.incrementExecuted();
        metricsService.incrementAuthorCompleted(command.getAuthor());
        // здесь можно расширить логику исполнения
    }

    public int getQueueSize() {
        return queue.size();
    }

    public int getMaxQueueSize() {
        return queue.getMaxSize();
    }

    public int getExecutedCommands() {
        return queue.getExecutedCommands();
    }

    public Queue<Command> getCommands() {
        return queue.getCurrentQueue();
    }
}