package com.parkmanshift.backend.domain.model;

import java.time.LocalDateTime;

public class SkeletonLog {
    private Long id;
    private String message;
    private LocalDateTime timestamp;

    public SkeletonLog() {}

    public SkeletonLog(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public SkeletonLog(Long id, String message, LocalDateTime timestamp) {
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
