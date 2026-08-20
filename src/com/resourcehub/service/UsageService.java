package com.resourcehub.service;

import com.resourcehub.model.Bill;
import com.resourcehub.model.Resource;
import com.resourcehub.model.Service;
import com.resourcehub.model.SessionStatus;
import com.resourcehub.model.UsageSession;
import com.resourcehub.store.InMemoryStore;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UsageService {
    private final InMemoryStore store;
    private final BillingService billingService;
    private final Object lock = new Object();

    public UsageService(InMemoryStore store, BillingService billingService) {
        this.store = store;
        this.billingService = billingService;
    }

    public UsageSession startUsage(String resourceId, String serviceId, String userName) {
        synchronized (lock) {
            Resource resource = store.findResource(resourceId)
                    .orElseThrow(() -> new IllegalArgumentException("resource not found"));

            Service service = store.findService(serviceId)
                    .orElseThrow(() -> new IllegalArgumentException("service not found"));

            if (!service.getResourceId().equals(resourceId)) {
                throw new IllegalArgumentException("that service is not for this resource");
            }

            int currentlyUsing = store.countActiveSessionsForResource(resourceId);
            if (currentlyUsing >= resource.getCapacity()) {
                throw new IllegalStateException("resource is full (" + resource.getCapacity() + ")");
            }

            // same person shouldn't start twice on the same thing
            for (UsageSession s : store.listActiveSessionsForResource(resourceId)) {
                if (s.getUserName().equalsIgnoreCase(userName.trim())) {
                    throw new IllegalStateException("you already have an active session here");
                }
            }

            UsageSession session = new UsageSession(resourceId, serviceId, userName, Instant.now());
            store.saveSession(session);
            return session;
        }
    }

    public Bill stopUsage(String sessionId) {
        synchronized (lock) {
            UsageSession session = store.findSession(sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("session not found"));

            if (session.getStatus() == SessionStatus.COMPLETED) {
                throw new IllegalStateException("already stopped");
            }

            Resource resource = store.findResource(session.getResourceId())
                    .orElseThrow(() -> new IllegalStateException("resource missing"));
            Service service = store.findService(session.getServiceId())
                    .orElseThrow(() -> new IllegalStateException("service missing"));

            session.complete(Instant.now());
            store.saveSession(session);

            Bill bill = billingService.generateBill(session, resource, service);
            store.saveBill(bill);
            return bill;
        }
    }

    public List<Map<String, Object>> listActiveSessions() {
        Instant now = Instant.now();
        return store.listActiveSessions().stream()
                .map(s -> toMap(s, now))
                .collect(Collectors.toList());
    }

    private Map<String, Object> toMap(UsageSession session, Instant now) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", session.getId());
        m.put("resourceId", session.getResourceId());
        m.put("serviceId", session.getServiceId());
        store.findResource(session.getResourceId()).ifPresent(r -> m.put("resourceName", r.getName()));
        store.findService(session.getServiceId()).ifPresent(s -> {
            m.put("serviceName", s.getName());
            m.put("firstHourPriceInr", s.getFirstHourPriceInr());
            m.put("additionalHourPriceInr", s.getAdditionalHourPriceInr());
        });
        m.put("userName", session.getUserName());
        m.put("startTime", session.getStartTime().toString());
        m.put("endTime", session.getEndTime() == null ? null : session.getEndTime().toString());
        m.put("status", session.getStatus().name());
        long secs = session.durationSeconds(now);
        m.put("durationSeconds", secs);
        m.put("durationLabel", BillingService.formatDuration(secs));
        m.put("currency", "INR");
        return m;
    }
}
