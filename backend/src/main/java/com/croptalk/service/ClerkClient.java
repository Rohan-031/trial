package com.croptalk.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

/** Replacement for `users.getUserList()` from @clerk/clerk-sdk-node (Clerk Backend API, GET /v1/users). */
@Component
public class ClerkClient {
    private final RestClient restClient;
    private final String secretKey;

    public ClerkClient(RestClient.Builder builder, @Value("${clerk.secret-key:}") String secretKey) {
        this.restClient = builder.baseUrl("https://api.clerk.com").build();
        this.secretKey = secretKey;
    }

    /** Returns the JSON array of Clerk users (the SDK default page: 10 users, as in the original call). */
    public JsonNode getUserList() {
        return restClient.get()
                .uri("/v1/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                .retrieve()
                .body(JsonNode.class);
    }
}
