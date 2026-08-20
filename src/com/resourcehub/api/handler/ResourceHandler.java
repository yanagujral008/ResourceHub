package com.resourcehub.api.handler;

import com.resourcehub.api.HttpSupport;
import com.resourcehub.api.Json;
import com.resourcehub.model.Resource;
import com.resourcehub.model.ResourceType;
import com.resourcehub.service.ResourceService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResourceHandler implements HttpHandler {
    private final ResourceService resourceService;

    public ResourceHandler(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (HttpSupport.handleOptions(exchange)) {
                return;
            }

            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equalsIgnoreCase(method) && "/api/resources".equals(path)) {
                HttpSupport.sendJson(exchange, 200, resourceService.listWithAvailability());
                return;
            }

            if ("POST".equalsIgnoreCase(method) && "/api/resources".equals(path)) {
                Map<String, Object> body = Json.parseObject(HttpSupport.readBody(exchange));
                String name = Json.asString(body, "name");
                String type = Json.asString(body, "type");
                int capacity = Json.asInt(body, "capacity", 0);
                List<ResourceService.ServiceSpec> specs = readServices(body.get("services"));

                if (name == null || name.isBlank()) {
                    HttpSupport.sendError(exchange, 400, "name is required");
                    return;
                }
                if (type == null || type.isBlank()) {
                    HttpSupport.sendError(exchange, 400, "type is required");
                    return;
                }
                if (capacity < 1) {
                    HttpSupport.sendError(exchange, 400, "capacity must be at least 1");
                    return;
                }
                if (specs.isEmpty()) {
                    HttpSupport.sendError(exchange, 400, "at least one service is required");
                    return;
                }

                Resource created = resourceService.create(name, ResourceType.fromString(type), capacity, specs);
                HttpSupport.sendJson(exchange, 201, resourceService.toResourceView(created));
                return;
            }

            HttpSupport.sendError(exchange, 404, "Not found");
        } catch (IllegalArgumentException e) {
            HttpSupport.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            HttpSupport.sendError(exchange, 500, e.getMessage() == null ? "error" : e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static List<ResourceService.ServiceSpec> readServices(Object raw) {
        List<ResourceService.ServiceSpec> specs = new ArrayList<>();
        if (!(raw instanceof List<?> list) || list.isEmpty()) {
            return specs;
        }
        for (Object item : list) {
            if (!(item instanceof Map)) {
                throw new IllegalArgumentException("invalid service");
            }
            Map<String, Object> map = (Map<String, Object>) item;
            String serviceName = Json.asString(map, "name");
            double first = Json.asDouble(map, "firstHourPriceInr", -1);
            double extra = Json.asDouble(map, "additionalHourPriceInr", -1);
            if (serviceName == null || serviceName.isBlank()) {
                throw new IllegalArgumentException("service name required");
            }
            if (first < 0 || extra < 0) {
                throw new IllegalArgumentException("prices must be >= 0");
            }
            specs.add(new ResourceService.ServiceSpec(serviceName, first, extra));
        }
        return specs;
    }
}
