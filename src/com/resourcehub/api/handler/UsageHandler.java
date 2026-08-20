package com.resourcehub.api.handler;

import com.resourcehub.api.HttpSupport;
import com.resourcehub.api.Json;
import com.resourcehub.model.Bill;
import com.resourcehub.model.UsageSession;
import com.resourcehub.service.BillingService;
import com.resourcehub.service.UsageService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class UsageHandler implements HttpHandler {
    private final UsageService usageService;

    public UsageHandler(UsageService usageService) {
        this.usageService = usageService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (HttpSupport.handleOptions(exchange)) {
                return;
            }

            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equalsIgnoreCase(method) && "/api/usage/active".equals(path)) {
                HttpSupport.sendJson(exchange, 200, usageService.listActiveSessions());
                return;
            }

            if ("POST".equalsIgnoreCase(method) && "/api/usage/start".equals(path)) {
                Map<String, Object> body = Json.parseObject(HttpSupport.readBody(exchange));
                String resourceId = Json.asString(body, "resourceId");
                String serviceId = Json.asString(body, "serviceId");
                String userName = Json.asString(body, "userName");

                if (resourceId == null || resourceId.isBlank()) {
                    HttpSupport.sendError(exchange, 400, "resourceId is required");
                    return;
                }
                if (serviceId == null || serviceId.isBlank()) {
                    HttpSupport.sendError(exchange, 400, "serviceId is required");
                    return;
                }
                if (userName == null || userName.isBlank()) {
                    HttpSupport.sendError(exchange, 400, "userName is required");
                    return;
                }

                UsageSession session = usageService.startUsage(resourceId, serviceId, userName);
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("id", session.getId());
                resp.put("resourceId", session.getResourceId());
                resp.put("serviceId", session.getServiceId());
                resp.put("userName", session.getUserName());
                resp.put("startTime", session.getStartTime().toString());
                resp.put("status", session.getStatus().name());
                HttpSupport.sendJson(exchange, 201, resp);
                return;
            }

            if ("POST".equalsIgnoreCase(method) && "/api/usage/stop".equals(path)) {
                Map<String, Object> body = Json.parseObject(HttpSupport.readBody(exchange));
                String sessionId = Json.asString(body, "sessionId");
                if (sessionId == null || sessionId.isBlank()) {
                    HttpSupport.sendError(exchange, 400, "sessionId is required");
                    return;
                }
                Bill bill = usageService.stopUsage(sessionId);
                HttpSupport.sendJson(exchange, 200, BillingService.toBillView(bill));
                return;
            }

            HttpSupport.sendError(exchange, 404, "Not found");
        } catch (IllegalArgumentException e) {
            HttpSupport.sendError(exchange, 400, e.getMessage());
        } catch (IllegalStateException e) {
            HttpSupport.sendError(exchange, 409, e.getMessage());
        } catch (Exception e) {
            HttpSupport.sendError(exchange, 500, e.getMessage() == null ? "error" : e.getMessage());
        }
    }
}
