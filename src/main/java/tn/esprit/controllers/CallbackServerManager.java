package tn.esprit.controllers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.function.Consumer;

public class CallbackServerManager {
    private static CallbackServerManager instance;
    private HttpServer server;
    private static final int[] PORTS = {8090, 8091, 8092};
    private int currentPortIndex = 0;

    private CallbackServerManager() {}

    public static synchronized CallbackServerManager getInstance() {
        if (instance == null) {
            instance = new CallbackServerManager();
        }
        return instance;
    }

    public synchronized void start(Consumer<HttpExchange> callbackHandler) throws IOException {
        if (server != null) {
            System.out.println("Callback server already running on port " + PORTS[currentPortIndex]);
            return;
        }

        IOException lastException = null;
        for (int i = 0; i < PORTS.length; i++) {
            try {
                server = HttpServer.create(new InetSocketAddress(PORTS[currentPortIndex]), 0);
                server.createContext("/auth/google/callback", exchange -> callbackHandler.accept(exchange));
                server.setExecutor(null);
                server.start();
                System.out.println("Callback server started on port " + PORTS[currentPortIndex]);
                Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
                return;
            } catch (IOException e) {
                lastException = e;
                System.err.println("Failed to start server on port " + PORTS[currentPortIndex] + ": " + e.getMessage());
                currentPortIndex = (currentPortIndex + 1) % PORTS.length;
                try {
                    Thread.sleep(3000); // Wait before trying next port
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new IOException("Failed to start callback server on any port", lastException);
    }

    public synchronized void stop() {
        if (server != null) {
            System.out.println("Stopping callback server on port " + PORTS[currentPortIndex]);
            try {
                server.stop(0);
                Thread.sleep(3000); // Wait for port release
                System.out.println("Callback server stopped successfully");
            } catch (Exception e) {
                System.err.println("Error stopping callback server: " + e.getMessage());
            }
            server = null;
        }
    }

    public synchronized boolean isRunning() {
        return server != null;
    }

    public synchronized int getCurrentPort() {
        return PORTS[currentPortIndex];
    }
}