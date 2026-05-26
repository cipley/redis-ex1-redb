package com.cipley.submission.redis.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;

import java.time.Duration;

public class RedisConfig {
    private final RedisClient redisClient;

    public RedisConfig(ApplicationConfig applicationConfig) {
        this(applicationConfig, applicationConfig.getRedisHost(), applicationConfig.getRedisPort());
    }

    public RedisConfig(ApplicationConfig applicationConfig, String host, int port) {
        var uriBuilder = RedisURI.builder()
                .withHost(host)
                .withPort(port)
                .withTimeout(Duration.ofMillis(applicationConfig.getRedisTimeout()));

        String username = applicationConfig.getRedisUsername();
        String password = applicationConfig.getRedisPassword();

        if (!username.isBlank() || !password.isBlank()) {
            uriBuilder.withAuthentication(username, password);
        }

        this.redisClient = RedisClient.create(uriBuilder.build());
    }

    public RedisClient getRedisClient() {
        return redisClient;
    }

    public StatefulRedisConnection<String, String> connect() {
        return redisClient.connect();
    }

    public void shutdown() {
        redisClient.shutdown();
    }
}
