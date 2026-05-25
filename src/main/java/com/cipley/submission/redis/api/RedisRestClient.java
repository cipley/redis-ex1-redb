package com.cipley.submission.redis.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class RedisRestClient {
    private static final Logger logger = LoggerFactory.getLogger(RedisRestClient.class);

    private final HttpClient httpClient;
    private final String baseUrl;
    private final String basicAuth;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final String APPLICATION_JSON = "application/json";

    public RedisRestClient(String host, int port, String username, String password) {
        this.baseUrl = host + ":" + port + "/v1";
        this.basicAuth = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

        // Bypass self-signed cert
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.httpClient = HttpClient.newBuilder()
                .sslContext(sslContext)
                .build();
    }

    public JsonNode get(String path) throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Accept", APPLICATION_JSON)
                .header("Authorization", basicAuth)
                .GET()
                .build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (!String.valueOf(response.statusCode()).startsWith("2")) {
            var errorResponse = toJson(response.body());
            if (errorResponse.get("description") != null) {
                throw new RuntimeException(errorResponse.get("description").toString());
            }
        }
        return toJson(response.body());
    }

    public JsonNode post(String path, Object body) throws Exception {
        var json = mapper.writeValueAsString(body);
        logger.debug("Request body: {}", json);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Accept", APPLICATION_JSON)
                .header("Content-Type", APPLICATION_JSON)
                .header("Authorization", basicAuth)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (!String.valueOf(response.statusCode()).startsWith("2")) {
            var errorResponse = toJson(response.body());
            if (errorResponse.get("description") != null) {
                throw new RuntimeException(errorResponse.get("description").toString());
            }
        }
        logger.debug("Response body: {}", response.body());
        return toJson(response.body());
    }

    public JsonNode delete(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Accept", APPLICATION_JSON)
                .header("Authorization", basicAuth)
                .DELETE()
                .build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (!String.valueOf(response.statusCode()).startsWith("2")) {
            var errorResponse = toJson(response.body());
            if (errorResponse.get("description") != null) {
                throw new RuntimeException(errorResponse.get("description").toString());
            }
        }
        return toJson(response.body());
    }

    public JsonNode toJson(String raw) {
        return mapper.readTree(raw);
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
}
