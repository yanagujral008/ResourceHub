package com.resourcehub.api.handler;

import com.resourcehub.api.HttpSupport;
import com.resourcehub.service.BillingService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class BillHandler implements HttpHandler {
    private final BillingService billingService;

    public BillHandler(BillingService billingService) {
        this.billingService = billingService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (HttpSupport.handleOptions(exchange)) {
                return;
            }
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())
                    && "/api/bills".equals(exchange.getRequestURI().getPath())) {
                HttpSupport.sendJson(exchange, 200, billingService.listBills());
                return;
            }
            HttpSupport.sendError(exchange, 404, "Not found");
        } catch (Exception e) {
            HttpSupport.sendError(exchange, 500, e.getMessage() == null ? "error" : e.getMessage());
        }
    }
}
