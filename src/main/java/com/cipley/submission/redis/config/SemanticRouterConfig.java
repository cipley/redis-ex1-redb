package com.cipley.submission.redis.config;

import redis.clients.jedis.RedisClient;
import redis.clients.jedis.UnifiedJedis;

public class SemanticRouterConfig {
    private final UnifiedJedis jedis;

    public SemanticRouterConfig(ApplicationConfig config) {
        this.jedis = RedisClient.create(
                config.getUnifiedJedisHost(),
                config.getUnifiedJedisPort()
        );
    }

    public UnifiedJedis getUnifiedJedis() {
        return jedis;
    }
}
