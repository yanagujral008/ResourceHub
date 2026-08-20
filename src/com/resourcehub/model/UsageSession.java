package com.resourcehub.model;

import java.time.Instant;
import java.util.UUID;

// one user using one resource under one service
public class UsageSession {
    private final String id;
    private final String resourceId;
    private final String serviceId;
    private final String userName;
    private final Instant startTime;
    private Instant endTime;
    private SessionStatus status;

    public UsageSession(String resourceId, String serviceId, String userName, Instant startTime) {
        if (userName == null || userName.isBlank()) {
            throw new IllegalArgumentException("user name required");
        }
        this.id = UUID.randomUUID().toString();
        this.resourceId = resourceId;
        this.serviceId = serviceId;
        this.userName = userName.trim();
        this.startTime = startTime;
        this.endTime = null;
        this.status = SessionStatus.ACTIVE;
    }

    public String getId() {
        return id;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getUserName() {
        return userName;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void complete(Instant end) {
        if (status != SessionStatus.ACTIVE) {
            throw new IllegalStateException("already stopped");
        }
        if (end.isBefore(startTime)) {
            throw new IllegalArgumentException("end time is before start time");
        }
        this.endTime = end;
        this.status = SessionStatus.COMPLETED;
    }

    public long durationSeconds(Instant now) {
        Instant until = endTime != null ? endTime : now;
        long secs = until.getEpochSecond() - startTime.getEpochSecond();
        return secs < 0 ? 0 : secs;
    }
}
