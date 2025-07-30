package com.weyland.starter.synthetichumancorestarter.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public class Command {

    @NotBlank
    @Size(max = 1000)
    private String description;

    @NotNull
    private CommandPriority priority;

    @NotBlank
    @Size(max = 100)
    private String author;

    @NotBlank
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?([+-]\\d{2}:\\d{2}|Z)$",
            message = "time must be in ISO8601 format")
    private String time;

    public Command() {}

    public Command(String description, CommandPriority priority, String author, String time) {
        this.description = description;
        this.priority = priority;
        this.author = author;
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public CommandPriority getPriority() {
        return priority;
    }

    public String getAuthor() {
        return author;
    }

    public String getTime() {
        return time;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPriority(CommandPriority priority) {
        this.priority = priority;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Command{" +
                "description='" + description + '\'' +
                ", priority=" + priority +
                ", author='" + author + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}