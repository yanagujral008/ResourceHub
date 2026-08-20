package com.resourcehub.store;

import com.resourcehub.model.Bill;
import com.resourcehub.model.Resource;
import com.resourcehub.model.Service;
import com.resourcehub.model.SessionStatus;
import com.resourcehub.model.UsageSession;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// no database - just maps. data is gone after restart
public class InMemoryStore {
    private final Map<String, Resource> resources = new ConcurrentHashMap<>();
    private final Map<String, Service> services = new ConcurrentHashMap<>();
    private final Map<String, UsageSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Bill> bills = new ConcurrentHashMap<>();

    public void saveResource(Resource resource) {
        resources.put(resource.getId(), resource);
    }

    public Optional<Resource> findResource(String id) {
        return Optional.ofNullable(resources.get(id));
    }

    public List<Resource> listResources() {
        return List.copyOf(resources.values());
    }

    public void saveService(Service service) {
        services.put(service.getId(), service);
    }

    public Optional<Service> findService(String id) {
        return Optional.ofNullable(services.get(id));
    }

    // cheapest first hour first
    public List<Service> listServicesForResource(String resourceId) {
        return services.values().stream()
                .filter(s -> s.getResourceId().equals(resourceId))
                .sorted(Comparator
                        .comparingDouble(Service::getFirstHourPriceInr)
                        .thenComparingDouble(Service::getAdditionalHourPriceInr)
                        .thenComparing(Service::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public void saveSession(UsageSession session) {
        sessions.put(session.getId(), session);
    }

    public Optional<UsageSession> findSession(String id) {
        return Optional.ofNullable(sessions.get(id));
    }

    public List<UsageSession> listActiveSessions() {
        return sessions.values().stream()
                .filter(s -> s.getStatus() == SessionStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    public List<UsageSession> listActiveSessionsForResource(String resourceId) {
        List<UsageSession> result = new ArrayList<>();
        for (UsageSession s : sessions.values()) {
            if (s.getStatus() == SessionStatus.ACTIVE && s.getResourceId().equals(resourceId)) {
                result.add(s);
            }
        }
        return result;
    }

    public int countActiveSessionsForResource(String resourceId) {
        return listActiveSessionsForResource(resourceId).size();
    }

    public void saveBill(Bill bill) {
        bills.put(bill.getId(), bill);
    }

    public List<Bill> listBills() {
        List<Bill> result = new ArrayList<>(bills.values());
        result.sort((a, b) -> b.getGeneratedAt().compareTo(a.getGeneratedAt()));
        return result;
    }
}
