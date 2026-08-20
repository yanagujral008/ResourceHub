package com.resourcehub.model;

public enum ResourceType {
    MEETING_ROOM("Meeting Room"),
    GYM_EQUIPMENT("Gym Equipment"),
    WORKSTATION("Paid Workstation");

    private final String displayName;

    ResourceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ResourceType fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("type required");
        }
        String key = value.trim().toUpperCase().replace(' ', '_').replace('-', '_');
        try {
            return ResourceType.valueOf(key);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("unknown type: " + value);
        }
    }
}
