package com.cipley.submission.redis;

import com.cipley.submission.redis.config.ApplicationConfig;
import com.cipley.submission.redis.config.RedisConfig;

public class Application {
    public static void main(String[] args) {
        var appConfig = new ApplicationConfig();
        var redisConfig = new RedisConfig(appConfig);
    }
}
