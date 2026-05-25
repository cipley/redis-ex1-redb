package com.cipley.submission.redis.cli.screens.rest;

import com.cipley.submission.redis.api.RedisRestClient;
import com.cipley.submission.redis.cli.SplitLayout;
import com.cipley.submission.redis.cli.screens.BaseScreen;
import com.cipley.submission.redis.cli.screens.Screen;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class ListUsersScreen extends BaseScreen implements Screen {
    private static final Logger logger = LoggerFactory.getLogger(ListUsersScreen.class);

    private static final int COL_NAME  = 24;
    private static final int COL_EMAIL = 36;
    private static final int COL_ROLE  = 20;

    private final RedisRestClient restClient;
    private List<AttributedString> tableLines = new ArrayList<>();
    private String statusMessage = "";

    public ListUsersScreen(RedisRestClient restClient) {
        this.restClient = restClient;
        loadUsers();
    }

    private void loadUsers() {
        try {
            var response = restClient.get("/users");
            tableLines.clear();
            tableLines.add(header());
            tableLines.add(divider());

            if (response.isArray()) {
                for (var user : response) {
                    String name  = text(user, "name");
                    String email = text(user, "email");
                    String role  = text(user, "role");
                    tableLines.add(row(name, email, role));
                }
                logger.info("Loaded {} users", response.size());
            } else {
                statusMessage = "Unexpected response: " + response;
                logger.warn("Unexpected listUsers response: {}", response);
            }
        } catch (Exception e) {
            statusMessage = "Failed to load users: " + e.getMessage();
            logger.error("Failed to load users", e);
        }
    }

    @Override
    public void render(SplitLayout layout) {
        var lines = new ArrayList<AttributedString>();
        lines.add(title("  Redis Exercise - 3) Redis REST API - List Users"));
        lines.add(blank());
        lines.addAll(tableLines);
        lines.add(blank());
        lines.add(option("  [r]  Refresh"));
        lines.add(option("  [0]  Back"));
        lines.add(blank());
        if (!statusMessage.isBlank()) {
            lines.add(error("  " + statusMessage));
        }
        layout.setMainContent(lines);
    }

    @Override
    public Screen handleInput(String input, SplitLayout layout) {
        switch (input.trim().toLowerCase()) {
            case "r" -> {
                statusMessage = "";
                loadUsers();
                logger.info("Users refreshed");
            }
            case "0", "back", "quit" -> {
                return null;
            }
            default -> statusMessage = "Unknown option: " + input.trim();
        }
        return this;
    }

    @Override
    public String prompt() {
        return "choice: ";
    }

    // ── Table helpers ────────────────────────────────────────────────────────

    private AttributedString header() {
        return new AttributedStringBuilder()
                .style(AttributedStyle.BOLD.foreground(AttributedStyle.WHITE))
                .append("  " + pad("NAME", COL_NAME) + pad("EMAIL", COL_EMAIL) + pad("ROLE", COL_ROLE))
                .style(AttributedStyle.DEFAULT)
                .toAttributedString();
    }

    private AttributedString divider() {
        String line = "  " + "-".repeat(COL_NAME + COL_EMAIL + COL_ROLE);
        return new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE))
                .append(line)
                .style(AttributedStyle.DEFAULT)
                .toAttributedString();
    }

    private AttributedString row(String name, String email, String role) {
        return new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
                .append("  " + pad(name, COL_NAME) + pad(email, COL_EMAIL) + pad(role, COL_ROLE))
                .style(AttributedStyle.DEFAULT)
                .toAttributedString();
    }

    private String pad(String value, int width) {
        if (value.length() >= width) return value.substring(0, width - 1) + " ";
        return value + " ".repeat(width - value.length());
    }

    private String text(JsonNode node, String field) {
        var v = node.get(field);
        return v != null && !v.isNull() ? v.asString() : "-";
    }
}
