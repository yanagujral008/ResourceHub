package com.resourcehub.service;

import com.resourcehub.model.Bill;
import com.resourcehub.model.Resource;
import com.resourcehub.model.Service;
import com.resourcehub.model.UsageSession;
import com.resourcehub.store.InMemoryStore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BillingService {
    private final InMemoryStore store;

    public BillingService(InMemoryStore store) {
        this.store = store;
    }

    public Bill generateBill(UsageSession session, Resource resource, Service service) {
        long duration = session.durationSeconds(session.getEndTime());
        long hours = billableHours(duration);

        // amount = first hour + (hours-1) * extra hour
        double amount = service.getFirstHourPriceInr()
                + (hours - 1) * service.getAdditionalHourPriceInr();
        amount = Math.round(amount * 100.0) / 100.0; // 2 decimal places

        return new Bill(
                session.getId(),
                resource.getId(),
                resource.getName(),
                service.getId(),
                service.getName(),
                session.getUserName(),
                duration,
                hours,
                service.getFirstHourPriceInr(),
                service.getAdditionalHourPriceInr(),
                amount,
                Instant.now()
        );
    }

    // round up to next hour (even 1 second counts as 1 hour)
    public static long billableHours(long seconds) {
        if (seconds <= 0) {
            return 1;
        }
        return (seconds + 3599) / 3600;
    }

    public static String formatDuration(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        if (h > 0) {
            return h + "h " + m + "m " + s + "s";
        }
        if (m > 0) {
            return m + "m " + s + "s";
        }
        return s + "s";
    }

    // turn Bill into a map so we can send it as JSON
    public static Map<String, Object> toBillView(Bill bill) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", bill.getId());
        m.put("sessionId", bill.getSessionId());
        m.put("resourceId", bill.getResourceId());
        m.put("resourceName", bill.getResourceName());
        m.put("serviceId", bill.getServiceId());
        m.put("serviceName", bill.getServiceName());
        m.put("userName", bill.getUserName());
        m.put("durationSeconds", bill.getDurationSeconds());
        m.put("durationLabel", formatDuration(bill.getDurationSeconds()));
        m.put("billableHours", bill.getBillableHours());
        m.put("firstHourPriceInr", bill.getFirstHourPriceInr());
        m.put("additionalHourPriceInr", bill.getAdditionalHourPriceInr());
        m.put("amountInr", bill.getAmountInr());
        m.put("currency", "INR");
        m.put("generatedAt", bill.getGeneratedAt().toString());
        return m;
    }

    public List<Map<String, Object>> listBills() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Bill bill : store.listBills()) {
            list.add(toBillView(bill));
        }
        return list;
    }
}
