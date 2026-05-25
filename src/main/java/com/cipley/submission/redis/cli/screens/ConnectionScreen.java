package com.cipley.submission.redis.cli.screens;

import com.cipley.submission.redis.cli.SplitLayout;
import com.cipley.submission.redis.config.ApplicationConfig;
import com.cipley.submission.redis.config.RedisConfig;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class ConnectionScreen extends BaseScreen implements Screen {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionScreen.class);

    private final ApplicationConfig applicationConfig;

    private String host;
    private Integer port;
    private boolean isAwaitingPort = false;
    private String statusMessage = "";

    public ConnectionScreen(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
        this.host = applicationConfig.getRedisHost();
        this.port = applicationConfig.getRedisPort();
    }

    @Override
    public void render(SplitLayout layout) {
        var lines = new ArrayList<AttributedString>();
        lines.add(title("  Redis - Consultant Engineer Exercise — Connection Setup"));
        lines.add(blank());
        lines.add(info("  Default host : " + applicationConfig.getRedisHost()));
        lines.add(info("  Default port : " + applicationConfig.getRedisPort()));
        lines.add(blank());
        lines.add(info("  Current host : " + host));
        lines.add(info("  Current port : " + port));
        lines.add(blank());
        lines.add(hint("  Press ENTER to accept the default value."));
        if (!statusMessage.isBlank()) {
            lines.add(error("  " + statusMessage));
        }
        layout.setMainContent(lines);
    }

    @Override
    public Screen handleInput(String input, SplitLayout layout) {
        if (!isAwaitingPort) {
            // Handling host input
            if (!input.isBlank()) {
                host = input.trim();
            } else {
                host = applicationConfig.getRedisHost();
            }
            logger.info("Host set to: {}", host);
            isAwaitingPort = true;
        } else {
            // Handling port input
            if (!input.isBlank()) {
                try {
                    port = Integer.parseInt(input.trim());
                } catch (NumberFormatException e) {
                    logger.warn("Invalid port number: {}, keeping default: {}", input.trim(), port);
                }
            } else {
                port = applicationConfig.getRedisPort();
            }
            logger.info("Port set to: {}", port);
            // Try connect
            return tryConnect();
        }
        return this;    // Stay on this screen
    }

    @Override
    public String prompt() {
        if (!isAwaitingPort) {
            return String.format("host [%s]: ", applicationConfig.getRedisHost());
        } else  {
            return String.format("port [%s]: ", applicationConfig.getRedisPort());
        }
    }

    private Screen tryConnect() {
        logger.info("Attempting to connect to Redis at {}:{}", host, port);
        var redisConfig = new RedisConfig(applicationConfig, host, port);

        try {
            var connection = redisConfig.connect();
            var pong = connection.sync().ping();
            logger.info("Redis PING -> {}", pong);
            return new MainMenuScreen(redisConfig, connection);
        } catch (Exception e) {
            logger.error("Failed to connect to {}:{} — {}", host, port, e.getMessage());
            redisConfig.shutdown();
            isAwaitingPort = false;
            statusMessage = "Connection failed: " + e.getMessage() + " — please try again.";
            return this;
        }
    }
}
