package com.cipley.submission.redis.config;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

public class ApplicationConfig {
    private final String redisHost;
    private final int redisPort;
    private final String redisUsername;
    private final String redisPassword;
    private final int redisTimeout;

    private final String restApiHost;
    private final int restApiPort;
    private final String restApiUsername;
    private final String restApiPassword;

    private final String unifiedJedisHost;
    private final int unifiedJedisPort;
    private final String unifiedJedisUsername;
    private final String unifiedJedisPassword;


    public String getRedisHost() {
        return redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public String getRedisUsername() {
        return redisUsername;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public int getRedisTimeout() {
        return redisTimeout;
    }

    public String getRestApiHost() {
        return restApiHost;
    }

    public int getRestApiPort() {
        return restApiPort;
    }

    public String getRestApiUsername() {
        return restApiUsername;
    }

    public String getRestApiPassword() {
        return restApiPassword;
    }

    public String getUnifiedJedisHost() {
        return unifiedJedisHost;
    }

    public int getUnifiedJedisPort() {
        return unifiedJedisPort;
    }

    public String getUnifiedJedisUsername() {
        return unifiedJedisUsername;
    }

    public String getUnifiedJedisPassword() {
        return unifiedJedisPassword;
    }

    @SuppressWarnings("unchecked")
    public ApplicationConfig() {
        Yaml yaml = new Yaml();

        // Check for external application.yml next to the jar first
        InputStream in = null;
        File external = new File("application.yml");
        try {
            if (!external.exists()) {
                in = new FileInputStream(external);
            } else {
                in = getClass().getClassLoader().getResourceAsStream("application.yml");
            }

            if (in == null) throw new RuntimeException("application.yml not found.");
            var config = (Map<String, Object>) yaml.load(in);
            var redis = (Map<String, Object>) config.get("redis");
            var restApi = (Map<String, Object>) config.get("rest-api");
            var unifiedJedis = (Map<String, Object>) config.get("unified-jedis");

            this.redisHost = (String) redis.getOrDefault("host", "localhost");
            this.redisPort = (int) redis.getOrDefault("port", 6379);
            this.redisUsername = (String) redis.getOrDefault("username", "default");
            this.redisPassword = (String) redis.getOrDefault("password", "");
            this.redisTimeout = (int) redis.getOrDefault("timeout", 5000);

            this.restApiHost = (String) restApi.getOrDefault("host", "localhost");
            this.restApiPort = (int) restApi.getOrDefault("port", 9443);
            this.restApiUsername = (String) restApi.getOrDefault("username", "admin");
            this.restApiPassword = (String) restApi.getOrDefault("password", "");

            this.unifiedJedisHost = (String) unifiedJedis.getOrDefault("host", "localhost");
            this.unifiedJedisPort = (int) unifiedJedis.getOrDefault("port", 6379);
            this.unifiedJedisUsername = (String) unifiedJedis.getOrDefault("username", "default");
            this.unifiedJedisPassword = (String) unifiedJedis.getOrDefault("password", "");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load application.yml.", e);
        }
    }
}
