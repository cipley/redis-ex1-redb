package com.cipley.submission.redis.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class ApplicationConfig {
    private final String redisHost;
    private final int redisPort;
    private final String redisPassword;
    private final int redisTimeout;

    public String getRedisHost() {
        return redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public int getRedisTimeout() {
        return redisTimeout;
    }

    @SuppressWarnings("unchecked")
    public ApplicationConfig() {
        Yaml yaml = new Yaml();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("application.yml")) {
            if (in == null) throw new RuntimeException("application.yml not found.");
            var config = (Map<String, Object>) yaml.load(in);
            var redis = (Map<String, Object>) config.get("redis");

            this.redisHost = (String) redis.getOrDefault("host", "localhost");
            this.redisPort = (int) redis.getOrDefault("port", 6379);
            this.redisPassword = (String) config.getOrDefault("password", "");
            this.redisTimeout = (int) config.getOrDefault("timeout", 5000);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load application.yml.", e);
        }
    }
}
