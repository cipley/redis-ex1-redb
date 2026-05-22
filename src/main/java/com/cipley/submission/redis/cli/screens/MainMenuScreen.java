package com.cipley.submission.redis.cli.screens;

import com.cipley.submission.redis.cli.SplitLayout;
import com.cipley.submission.redis.config.ApplicationConfig;
import com.cipley.submission.redis.config.RedisConfig;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class MainMenuScreen implements Screen {
    private static final Logger logger = LoggerFactory.getLogger(MainMenuScreen.class);

    private final RedisConfig redisConfig;
    private final StatefulRedisConnection<String, String> connection;
    private final RedisCommands<String, String> commands;
    private String statusMessage = "";

    public MainMenuScreen(RedisConfig redisConfig, StatefulRedisConnection<String, String> connection) {
        this.redisConfig = redisConfig;
        this.connection = connection;
        this.commands = connection.sync();
        logger.info("Connected to Redis");
    }

    @Override
    public void render(SplitLayout layout) {
        var lines = new ArrayList<AttributedString>();
        lines.add(blank());
        lines.add(title("  Redis - Consultant Engineer Exercise — Main Menu"));
        lines.add(blank());
        lines.add(blank());
        lines.add(option("  [1]  Redis Database operations"));
        lines.add(option("  [2]  Redis REST APIs"));
        lines.add(option("  [3]  Semantic Router"));
        lines.add(blank());
        lines.add(option("  [0]  Exit"));
        lines.add(blank());
        if (!statusMessage.isBlank()) {
            lines.add(status("  " + statusMessage));
        }
        layout.setMainContent(lines);
    }

    @Override
    public Screen handleInput(String input, SplitLayout layout) {
        switch (input.trim()) {
            case "1" -> {
                logger.info("User selected option 1");
                statusMessage = "Option 1 selected — (placeholder)";
            }
            case "2" -> {
                logger.info("User selected option 2");
                statusMessage = "Option 2 selected — (placeholder)";
            }
            case "3" -> {
                logger.info("User selected option 3");
                statusMessage = "Option 3 selected — (placeholder)";
            }
            case "0", "exit", "quit" -> {
                connection.close();
                redisConfig.shutdown();
                logger.info("User exited from main menu");
                return null; // signals shell to exit
            }
            default -> {
                logger.warn("Unknown menu input: {}", input);
                statusMessage = "Unknown option: " + input;
            }
        }
        return this;
    }

    @Override
    public String prompt() {
        return "choice: ";
    }

    // --- Styling helpers ---
    private AttributedString title(String text) {
        return new AttributedStringBuilder()
                .style(AttributedStyle.BOLD.foreground(AttributedStyle.CYAN))
                .append(text)
                .style(AttributedStyle.DEFAULT)
                .toAttributedString();
    }

    private AttributedString option(String text) {
        return new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
                .append(text)
                .style(AttributedStyle.DEFAULT)
                .toAttributedString();
    }

    private AttributedString status(String text) {
        return new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
                .append(text)
                .style(AttributedStyle.DEFAULT)
                .toAttributedString();
    }

    private AttributedString blank() {
        return new AttributedString("");
    }
}
