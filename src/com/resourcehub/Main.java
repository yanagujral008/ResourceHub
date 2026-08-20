package com.resourcehub;

import com.resourcehub.api.handler.BillHandler;
import com.resourcehub.api.handler.ResourceHandler;
import com.resourcehub.api.handler.StaticHandler;
import com.resourcehub.api.handler.UsageHandler;
import com.resourcehub.service.BillingService;
import com.resourcehub.service.ResourceService;
import com.resourcehub.service.UsageService;
import com.resourcehub.store.InMemoryStore;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;

// starts the http server.
public class Main {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        InMemoryStore store = new InMemoryStore();
        ResourceService resources = new ResourceService(store);
        BillingService billing = new BillingService(store);
        UsageService usage = new UsageService(store, billing);
        resources.seedDefaults();

        Path webDir = Paths.get("web").toAbsolutePath();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/resources", new ResourceHandler(resources));
        server.createContext("/api/usage", new UsageHandler(usage));
        server.createContext("/api/bills", new BillHandler(billing));
        server.createContext("/", new StaticHandler(webDir));

        server.start();
        System.out.println("running on http://localhost:" + port + "/");
        System.out.println("ctrl+c to stop");
    }
}
