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

public class CreateNewUserScreen extends BaseScreen implements Screen {
    private static final Logger logger = LoggerFactory.getLogger(CreateNewUserScreen.class);

    private final RedisRestClient redisRestClient;
    private String userName;
    private String email;
    private String role;
    private String password;
    private String statusMessage = "";
    private boolean isInputComplete = false;

    public CreateNewUserScreen(RedisRestClient redisRestClient) {
        this.redisRestClient = redisRestClient;
    }

    @Override
    public void render(SplitLayout layout) {
        var lines = new ArrayList<AttributedString>();
        lines.add(title("  Redis Exercise - 2) Redis REST API - Create New User"));
        lines.add(blank());
        lines.add(option("  [0]  Back"));
        lines.add(blank());
        lines.add(blank());
        if (!statusMessage.isBlank()) {
            lines.addAll(wrap(statusMessage, layout.getWidth() - 4));
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

        if (userName == null || userName.isBlank()) {
            // Handling user name
            userName = input.trim();
            logger.info("User name set to {}", userName);
        } else if (email == null || email.isBlank()) {
            // Handling email
            email = input.trim();
            logger.info("Email set to {}", email);
        } else if (password == null || password.isBlank()) {
            // Handling Password
            password = input.trim();
            logger.info("Password set.");
        } else if (role == null || role.isBlank()) {
            // Handling Role
            role = input.trim();
            logger.info("Role set to {}", role);
            isInputComplete = true;
        }
        if (isInputComplete) {
            createUser();
            return null;
        }
        return this;
    }

    @Override
    public String prompt() {
        if (userName == null || userName.isBlank()) {
            return "Enter user name: ";
        } else if (email == null || email.isBlank()) {
            return "Enter email address: ";
        } else if (password == null || password.isBlank()) {
            return "Enter password: ";
        } else {
            return "Enter role: ";
        }
    }

    private void createUser() {
        logger.info("Calling Redis API to create user...");
        var request = new CreateUserRequest(userName, password, email, role);
        try {
            var path = "/users";
            var response = redisRestClient.post(path, request);
            var uid = response.get("uid");
            var createdName = response.get("name");
            statusMessage = "Successfully created User " + createdName + "with uid " + uid;
            logger.info("Successfully created User {} with uid {}", createdName, uid);
        } catch (Exception e) {
            logger.error("Failed to create new user!", e);
            statusMessage = "Failed to create new user!";
        }
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    record CreateUserRequest(String name, String password, String email, String role) {}

}
