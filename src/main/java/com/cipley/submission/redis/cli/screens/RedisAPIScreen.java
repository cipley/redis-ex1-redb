package com.cipley.submission.redis.cli.screens;

import com.cipley.submission.redis.api.RedisRestClient;
import com.cipley.submission.redis.cli.SplitLayout;
import com.cipley.submission.redis.cli.screens.rest.CreateNewDatabaseScreen;
import com.cipley.submission.redis.cli.screens.rest.CreateNewUserScreen;
import com.cipley.submission.redis.cli.screens.rest.DeleteDatabaseScreen;
import com.cipley.submission.redis.cli.screens.rest.ListUsersScreen;
import com.cipley.submission.redis.config.ApplicationConfig;
import org.jline.utils.AttributedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class RedisAPIScreen extends BaseScreen implements Screen {
    private static final Logger logger = LoggerFactory.getLogger(RedisAPIScreen.class);
    private final RedisRestClient redisRestClient;
    private final ApplicationConfig applicationConfig;
    private String statusMessage = "";

    public RedisAPIScreen() {
        this.applicationConfig = new ApplicationConfig();
        this.redisRestClient = new RedisRestClient(
                applicationConfig.getRestApiHost(),
                applicationConfig.getRestApiPort(),
                applicationConfig.getRestApiUsername(),
                applicationConfig.getRestApiPassword()
        );
    }

    @Override
    public void render(SplitLayout layout) {
        var lines = new ArrayList<AttributedString>();
        lines.add(title("  Redis Exercise - 2) Redis REST API"));
        lines.add(blank());
        lines.add(blank());
        lines.add(option("  [1]  Create New Database"));
        lines.add(option("  [2]  Create New User"));
        lines.add(option("  [3]  List Users"));
        lines.add(option("  [4]  Delete Database"));
        lines.add(blank());
        lines.add(option("  [0]  Back"));
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
                statusMessage = "Option 1 selected — Create New Database";
                return new CreateNewDatabaseScreen(redisRestClient);
            }
            case "2" -> {
                logger.info("User selected option 2");
                statusMessage = "Option 2 selected — Create New User";
                return new CreateNewUserScreen(redisRestClient);
            }
            case "3" -> {
                logger.info("User selected option 3");
                statusMessage = "Option 3 selected — List Users";
                return new ListUsersScreen(redisRestClient);
            }
            case "4" -> {
                logger.info("User selected option 4");
                statusMessage = "Option 4 selected — Delete Database";
                return new DeleteDatabaseScreen(redisRestClient);
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
}
