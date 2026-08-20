package com.resourcehub.model;

import java.util.UUID;

// meeting room / gym machine / workstation
public class Resource {
    private final String id;
    private final String name;
    private final ResourceType type;
    private final int capacity;

    public Resource(String name, ResourceType type, int capacity) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name required");
        }
        if (type == null) {
            throw new IllegalArgumentException("type required");
        }
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity should be at least 1");
        }
        this.id = UUID.randomUUID().toString();
        this.name = name.trim();
        this.type = type;
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ResourceType getType() {
        return type;
    }

    public int getCapacity() {
        return capacity;
    }
}
