package com.pratyush.docsearch.server;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import com.pratyush.docsearch.model.Result;
import com.pratyush.docsearch.model.SerializationUtils;

public class WebClient {
    private HttpClient client;

    public WebClient() {
        this.client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    }

    public CompletableFuture<Result> sendTask(String url, byte[] payload) {
        HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
            .uri(URI.create(url))
            .build();

            CompletableFuture<Result> res =  client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
            .thenApply(HttpResponse::body)
            .thenApply(responseBody -> (Result) SerializationUtils.deserialize(responseBody));
            System.out.println("RES");
            System.out.println(res);
            return res;
    }
}
