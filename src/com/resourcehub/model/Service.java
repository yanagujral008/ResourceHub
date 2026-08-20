package com.resourcehub.model;

import java.util.UUID;

// pricing plan for a resource (all prices in Rs)
// first hour is one rate, extra hours are another
public class Service {
    private final String id;
    private final String resourceId;
    private final String name;
    private final double firstHourPriceInr;
    private final double additionalHourPriceInr;

    public Service(String resourceId, String name, double firstHour, double extraHour) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("service name required");
        }
        if (firstHour < 0 || extraHour < 0) {
            throw new IllegalArgumentException("prices cannot be negative");
        }
        this.id = UUID.randomUUID().toString();
        this.resourceId = resourceId;
        this.name = name.trim();
        this.firstHourPriceInr = firstHour;
        this.additionalHourPriceInr = extraHour;
    }

    public String getId() {
        return id;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getName() {
        return name;
    }

    public double getFirstHourPriceInr() {
        return firstHourPriceInr;
    }

    public double getAdditionalHourPriceInr() {
        return additionalHourPriceInr;
    }
}
