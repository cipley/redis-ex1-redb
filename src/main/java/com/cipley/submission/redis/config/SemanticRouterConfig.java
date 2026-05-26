package com.cipley.submission.redis.config;

import redis.clients.jedis.RedisClient;
import redis.clients.jedis.UnifiedJedis;

public class SemanticRouterConfig {
    private final UnifiedJedis jedis;

    public SemanticRouterConfig(ApplicationConfig config) {
        this.jedis = RedisClient.create(
                config.getUnifiedJedisHost(),
                config.getUnifiedJedisPort(),
                config.getUnifiedJedisUsername(),
                config.getUnifiedJedisPassword()
        );
    }

    public UnifiedJedis getUnifiedJedis() {
        return jedis;
    }
}
