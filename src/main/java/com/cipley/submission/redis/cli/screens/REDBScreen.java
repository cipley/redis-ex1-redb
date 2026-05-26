package com.cipley.submission.redis.cli.screens;

import com.cipley.submission.redis.cli.SplitLayout;
import io.lettuce.core.KeyValue;
import io.lettuce.core.api.sync.RedisCommands;
import org.jline.utils.AttributedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class REDBScreen extends BaseScreen implements Screen {
    private static final Logger logger = LoggerFactory.getLogger(REDBScreen.class);

    private final RedisCommands<String, String> commands;
    private String statusMessage = "";

    public REDBScreen(RedisCommands<String, String> commands) {
        this.commands = commands;
    }

    @Override
    public void render(SplitLayout layout) {
        var lines = new ArrayList<AttributedString>();
        lines.add(title("  Redis Exercise - 1) Redis Database"));
        lines.add(blank());
        lines.add(blank());
        lines.add(option("  [1]  Check REDB values, print in reverse order"));
        lines.add(option("  [2]  Insert values 1-100"));
        lines.add(option("  [3]  Clear values"));
        lines.add(blank());
        lines.add(option("  [0]  Back"));
        lines.add(blank());
        if (!statusMessage.isBlank()) {
            lines.addAll(wrap(statusMessage, layout.getWidth() - 4));
        }
        layout.setMainContent(lines);
    }

    @Override
    public Screen handleInput(String input, SplitLayout layout) {
        switch (input.trim()) {
            case "1" -> {
                logger.info("User selected option 1");
                statusMessage = "Read values, print in reverse...";
                statusMessage = readValues();
            }
            case "2" -> {
                logger.info("User selected option 2");
                statusMessage = "Inserting value 1-100 to Redis...";
                statusMessage = insertHundredValues();
            }
            case "3" -> {
                logger.info("User selected option 3");
                statusMessage = "Clearing values inside Redis...";
                statusMessage = clearDatabase();
            }
            case "0", "exit", "quit" -> {
                logger.info("User going back to main menu");
                return null;
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

    private String readValues() {
        try {
            // get all keys first
            var keys = commands.keys("exercise:*");
            if (keys.isEmpty()) {
                return "Redis Database doesn't contain exercise items.";
            }
            // use MGET
            var keyArray = keys.toArray(new String[0]);
            return commands.mget(keyArray)
                    .stream()
                    .filter(KeyValue::hasValue)
                    .map(KeyValue::getValue)
                    .sorted(Comparator.comparingInt((String s) -> Integer.parseInt(s)).reversed())
                    .collect(Collectors.joining("; "));
        } catch (Exception e) {
            logger.error("Failed reading values from Redis!", e);
            return "Failed reading values from Redis!";
        }
    }

    /**
     * Function to insert 1 to 100
     */
    private String insertHundredValues() {
        try {
            var kv = IntStream.rangeClosed(1, 100)
                    .boxed()
                    .collect(Collectors.toMap(integer ->
                            String.join(":", "exercise", String.valueOf(integer)),    // key
                            String::valueOf     // value
                    ));
            commands.mset(kv);
            return "Success inserting values to Redis.";
        } catch (Exception e) {
            logger.error("Failed to insert values to Redis!", e);
            return "Failed to insert values to Redis!";
        }
    }

    /**
     * Function to clear database
     */
    private String clearDatabase() {
        try {
            //Collect keys
            var keys = commands.keys("exercise:*")
                    .toArray(new String[0]);
            commands.unlink(keys);
            return "Success clearing Redis for exercise values.";
        }  catch (Exception e) {
            logger.error("Failed to clear Redis!", e);
            return "Failed to clear Redis!";
        }
    }
}
