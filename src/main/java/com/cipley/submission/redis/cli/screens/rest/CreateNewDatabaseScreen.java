package com.cipley.submission.redis.cli.screens.rest;

import com.cipley.submission.redis.api.RedisRestClient;
import com.cipley.submission.redis.cli.SplitLayout;
import com.cipley.submission.redis.cli.screens.BaseScreen;
import com.cipley.submission.redis.cli.screens.Screen;
import org.jline.utils.AttributedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.ArrayList;

public class CreateNewDatabaseScreen extends BaseScreen implements Screen {
    private static final Logger logger = LoggerFactory.getLogger(CreateNewDatabaseScreen.class);

    private final RedisRestClient redisRestClient;
    private String databaseName;
    private Integer memorySize;
    private String statusMessage = "";
    private boolean isInputComplete = false;

    public CreateNewDatabaseScreen(RedisRestClient redisRestClient) {
        this.redisRestClient = redisRestClient;
    }

    @Override
    public void render(SplitLayout layout) {
        var lines = new ArrayList<AttributedString>();
        lines.add(title("  Redis Exercise - 1) Redis REST API - Create New Database"));
        lines.add(blank());
        lines.add(option("  [0]  Back"));
        lines.add(blank());
        lines.add(blank());
        if (!statusMessage.isBlank()) {
            lines.add(status("  " + statusMessage));
        }
        layout.setMainContent(lines);
    }

    @Override
    public Screen handleInput(String input, SplitLayout layout) {
        if (input.isBlank()) {
            statusMessage = "Input cannot be blank";
        } else if (input.equals("0")) {
            logger.info("User going back to main menu");
            return null;
        }

        if (databaseName == null || databaseName.isBlank()) {
            // Handling database name
            databaseName = input.trim();
            logger.info("Database name set to {}", databaseName);
        }
        else if (memorySize == null || memorySize <= 0) {
            // Handling memory size
            memorySize = Integer.parseInt(input.trim());
            logger.info("Memory size set to {}", memorySize);
            isInputComplete = true;
        }
        if(isInputComplete) {
            createDatabase();
            return null;
        }
        return this;
    }

    @Override
    public String prompt() {
        if (databaseName == null || databaseName.isBlank()) {
            return "Enter database name: ";
        } else {
            return "Enter database memory size in bytes: ";
        }
    }

    private void createDatabase() {
        logger.info("Calling Redis API to create database...");
        var request = new CreateDatabaseRequest(databaseName, memorySize);
        try {
            var path = "/bdbs";
            var response = redisRestClient.post(path, request);
            var uid = response.get("uid");
            var createdName = response.get("name");
            statusMessage = "Successfully created database " + createdName + "with uid " + uid;
            logger.info("Successfully created database {} with uid {}", createdName, uid);
        } catch (Exception e) {
            logger.error("Failed to create new database!", e);
            statusMessage = "Failed to create new database!";
        }
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    record CreateDatabaseRequest(String name, int memorySize) {}

}
