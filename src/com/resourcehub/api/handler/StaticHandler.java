package com.resourcehub.api.handler;

import com.resourcehub.api.HttpSupport;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StaticHandler implements HttpHandler {
    private final Path webRoot;

    public StaticHandler(Path webRoot) {
        this.webRoot = webRoot;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpSupport.sendError(exchange, 405, "Method not allowed");
            return;
        }

        String raw = exchange.getRequestURI().getPath();
        if (raw == null || raw.equals("/") || raw.isBlank()) {
            raw = "/index.html";
        }

        String relative = raw.startsWith("/") ? raw.substring(1) : raw;
        Path file = webRoot.resolve(relative).normalize();
        if (!file.startsWith(webRoot.normalize())) {
            HttpSupport.sendError(exchange, 403, "Forbidden");
            return;
        }

        if (!Files.exists(file) || Files.isDirectory(file)) {
            HttpSupport.sendError(exchange, 404, "File not found");
            return;
        }

        HttpSupport.sendBytes(exchange, 200, typeOf(file.getFileName().toString()), Files.readAllBytes(file));
    }

    private static String typeOf(String name) {
        String n = name.toLowerCase();
        if (n.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (n.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (n.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        return "application/octet-stream";
    }
}
