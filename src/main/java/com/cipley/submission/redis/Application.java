package com.cipley.submission.redis;

import com.cipley.submission.redis.cli.CommandHandler;
import com.cipley.submission.redis.cli.ConsoleShell;
import com.cipley.submission.redis.config.ApplicationConfig;
import com.cipley.submission.redis.config.RedisConfig;
import io.lettuce.core.api.StatefulRedisConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    public static void main(String[] args) {
        logger.info("Starting Application");

        var appConfig = new ApplicationConfig();

        try {
            ConsoleShell shell = new ConsoleShell(appConfig);
            shell.start();
        } catch (Exception ex) {
            // Print directly to stderr so it's always visible regardless of UI state
            ex.printStackTrace(System.err);
            logger.error("Fatal error starting application", ex);
        }

        logger.info("Shutdown completed.");
    }
}
