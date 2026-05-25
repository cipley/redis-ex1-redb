package com.cipley.submission.redis.cli.screens.rest;

import com.cipley.submission.redis.api.RedisRestClient;
import com.cipley.submission.redis.cli.SplitLayout;
import com.cipley.submission.redis.cli.screens.BaseScreen;
import com.cipley.submission.redis.cli.screens.Screen;
import org.jline.utils.AttributedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class DeleteDatabaseScreen extends BaseScreen implements Screen {
    private static final Logger logger = LoggerFactory.getLogger(DeleteDatabaseScreen.class);

    private final RedisRestClient redisRestClient;
    private int uid;
    private String statusMessage = "";

    public DeleteDatabaseScreen(RedisRestClient redisRestClient) {
        this.redisRestClient = redisRestClient;
    }

    @Override
    public void render(SplitLayout layout) {
        var lines = new ArrayList<AttributedString>();
        lines.add(title("  Redis Exercise - 4) Redis REST API - Delete Database"));
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
            logger.info("User is going back to REST API menu.");
            return null;
        } else {
            uid = Integer.parseInt(input.trim());
            deleteDatabase();
            return null;
        }
        return this;
    }

    @Override
    public String prompt() {
        return "Enter database uid to delete: ";
    }

    private void deleteDatabase() {
        logger.info("Calling Redis API to delete database...");
        try {
            var path = "/bdbs/" + uid;
            var response = redisRestClient.delete(path);
            statusMessage = "Successfully deleted database with uid " + uid;
            logger.info("Successfully delete database with uid {}", uid);
        } catch (Exception e) {
            logger.error("Failed to delete database!", e);
            statusMessage = "Failed to delete database!";
        }
    }
}
