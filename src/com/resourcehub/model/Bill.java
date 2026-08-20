package com.resourcehub.model;

import java.time.Instant;// for timestamp
import java.util.UUID;// for bill

// generated when a session is stopped
public class Bill {
    private final String id;
    private final String sessionId;
    private final String resourceId;
    private final String resourceName;
    private final String serviceId;
    private final String serviceName;
    private final String userName;
    private final long durationSeconds;
    private final long billableHours;
    private final double firstHourPriceInr;
    private final double additionalHourPriceInr;
    private final double amountInr;
    private final Instant generatedAt;

    public Bill(String sessionId, String resourceId, String resourceName,
                String serviceId, String serviceName, String userName,
                long durationSeconds, long billableHours,
                double firstHourPriceInr, double additionalHourPriceInr,
                double amountInr, Instant generatedAt) {
        this.id = UUID.randomUUID().toString();
        this.sessionId = sessionId;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.userName = userName;
        this.durationSeconds = durationSeconds;
        this.billableHours = billableHours;
        this.firstHourPriceInr = firstHourPriceInr;
        this.additionalHourPriceInr = additionalHourPriceInr;
        this.amountInr = amountInr;
        this.generatedAt = generatedAt;
    }

    public String getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getUserName() {
        return userName;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public long getBillableHours() {
        return billableHours;
    }

    public double getFirstHourPriceInr() {
        return firstHourPriceInr;
    }

    public double getAdditionalHourPriceInr() {
        return additionalHourPriceInr;
    }

    public double getAmountInr() {
        return amountInr;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }
}
