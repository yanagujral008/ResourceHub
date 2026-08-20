package com.resourcehub.service;

import com.resourcehub.model.Resource;
import com.resourcehub.model.ResourceType;
import com.resourcehub.model.Service;
import com.resourcehub.store.InMemoryStore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResourceService {
    private final InMemoryStore store;

    public ResourceService(InMemoryStore store) {
        this.store = store;
    }

    // helper for create() so we dont pass 4 separate lists around
    public static class ServiceSpec {
        public String name;
        public double firstHourPriceInr;
        public double additionalHourPriceInr;

        public ServiceSpec(String name, double firstHourPriceInr, double additionalHourPriceInr) {
            this.name = name;
            this.firstHourPriceInr = firstHourPriceInr;
            this.additionalHourPriceInr = additionalHourPriceInr;
        }
    }

    public Resource create(String name, ResourceType type, int capacity, List<ServiceSpec> specs) {
        if (specs == null || specs.isEmpty()) {
            throw new IllegalArgumentException("need at least one service");
        }
        Resource resource = new Resource(name, type, capacity);
        store.saveResource(resource);
        for (ServiceSpec spec : specs) {
            Service svc = new Service(resource.getId(), spec.name, spec.firstHourPriceInr, spec.additionalHourPriceInr);
            store.saveService(svc);
        }
        return resource;
    }

    public List<Map<String, Object>> listWithAvailability() {
        List<Resource> list = new ArrayList<>(store.listResources());
        list.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        List<Map<String, Object>> out = new ArrayList<>();
        for (Resource r : list) {
            int used = store.countActiveSessionsForResource(r.getId());
            int free = r.getCapacity() - used;
            if (free < 0) {
                free = 0;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", r.getId());
            row.put("name", r.getName());
            row.put("type", r.getType().name());
            row.put("typeLabel", r.getType().getDisplayName());
            row.put("capacity", r.getCapacity());
            row.put("activeUsers", used);
            row.put("availableSlots", free);
            row.put("isFull", free == 0);
            row.put("currency", "INR");
            row.put("services", toServiceViews(store.listServicesForResource(r.getId())));
            out.add(row);
        }
        return out;
    }

    public Map<String, Object> toResourceView(Resource r) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", r.getId());
        row.put("name", r.getName());
        row.put("type", r.getType().name());
        row.put("capacity", r.getCapacity());
        row.put("currency", "INR");
        row.put("services", toServiceViews(store.listServicesForResource(r.getId())));
        return row;
    }

    private List<Map<String, Object>> toServiceViews(List<Service> services) {
        List<Map<String, Object>> views = new ArrayList<>();
        for (Service s : services) {
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("id", s.getId());
            view.put("resourceId", s.getResourceId());
            view.put("name", s.getName());
            view.put("firstHourPriceInr", s.getFirstHourPriceInr());
            view.put("additionalHourPriceInr", s.getAdditionalHourPriceInr());
            view.put("currency", "INR");
            views.add(view);
        }
        return views;
    }

    public void seedDefaults() {
        if (!store.listResources().isEmpty()) {
            return;
        }
        // sample: first hour 30, extra 10
        create("Conference Room A", ResourceType.MEETING_ROOM, 8, List.of(
                new ServiceSpec("Hourly booking", 30, 10),
                new ServiceSpec("Half day", 100, 40)
        ));
        create("Small Meeting Room", ResourceType.MEETING_ROOM, 2, List.of(
                new ServiceSpec("Hourly", 20, 8)
        ));
        create("Treadmill 1", ResourceType.GYM_EQUIPMENT, 1, List.of(
                new ServiceSpec("Gym hourly", 15, 10)
        ));
        create("Rowing Machine", ResourceType.GYM_EQUIPMENT, 2, List.of(
                new ServiceSpec("Gym hourly", 12, 8)
        ));
        create(" Desk Area", ResourceType.WORKSTATION, 6, List.of(

                new ServiceSpec("Normal", 25, 12),
                new ServiceSpec("Premium", 40, 15)
        ));
        create("Private Desk", ResourceType.WORKSTATION, 1, List.of(
                new ServiceSpec("Desk hourly", 35, 15)
        ));
    }
}
