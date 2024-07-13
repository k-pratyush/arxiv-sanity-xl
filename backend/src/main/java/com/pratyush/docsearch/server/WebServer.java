package com.pratyush.docsearch.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;

public class WebServer {
    public static final String  STATUS_ENDPOINT = "/status";
    public final int port;
    private HttpServer server;
    public final OnRequestCallback onRequestCallback;

    public WebServer(int port, OnRequestCallback onRequestCallback) {
        this.port = port;
        this.onRequestCallback = onRequestCallback;
    }

    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }
        
        HttpContext statusContext = server.createContext(STATUS_ENDPOINT);
        HttpContext taskContext = server.createContext(onRequestCallback.getEndpoint());
        statusContext.setHandler(this::handleStatusCheckRequest);
        taskContext.setHandler(this::handleTaskRequest);

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }

    public void stop() {
        server.stop(10);
    }

    private void handleStatusCheckRequest(HttpExchange exchange) throws IOException {
        if(!(exchange.getRequestMethod().equalsIgnoreCase("get"))) {
            exchange.close();
            return;
        }

        String responseMessage = "Server is alive";
        sendResponse(responseMessage.getBytes(), exchange);
    }

    public void handleTaskRequest(HttpExchange exchange) throws IOException {
        if(!(exchange.getRequestMethod().equalsIgnoreCase("post"))) {
            exchange.close();
            return;
        }

        byte[] responseBytes = onRequestCallback.handleRequest(exchange.getRequestBody().readAllBytes());
        sendResponse(responseBytes, exchange);
    }

    private void sendResponse(byte[] responseMessage, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, responseMessage.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseMessage);
        outputStream.flush();
        outputStream.close();
    }

}
