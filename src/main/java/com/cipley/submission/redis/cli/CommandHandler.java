package com.cipley.submission.redis.cli;

import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.List;

public class CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);
    private final RedisCommands<String, String> redis;
    private static final List<String> COMMANDS = List.of(
            "set", "get", "del", "keys", "ping", "help", "exit"
    );

    public CommandHandler(RedisCommands<String, String> redis) {
        this.redis = redis;
    }

    public List<String> availableCommands() {
        return COMMANDS;
    }

    /**
     * @return true to keep the shell running, false to exit
     */
    public boolean handle(String input, PrintWriter out) {
        String[] parts = input.split("\\s+", 3);
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "set" -> {
                if (parts.length < 3) { out.println("Usage: set <key> <value>"); break; }
                redis.set(parts[1], parts[2]);
                logger.info("SET {} = {}", parts[1], parts[2]);
                out.println("OK");
            }
            case "get" -> {
                if (parts.length < 2) { out.println("Usage: get <key>"); break; }
                String val = redis.get(parts[1]);
                logger.info("GET {} -> {}", parts[1], val);
                out.println(val != null ? val : "(nil)");
            }
            case "del" -> {
                if (parts.length < 2) { out.println("Usage: del <key>"); break; }
                long removed = redis.del(parts[1]);
                logger.info("DEL {} -> {}", parts[1], removed);
                out.println("(deleted " + removed + ")");
            }
            case "keys" -> {
                String pattern = parts.length > 1 ? parts[1] : "*";
                var keys = redis.keys(pattern);
                logger.info("KEYS {} -> {} results", pattern, keys.size());
                keys.forEach(out::println);
            }
            case "ping" -> {
                out.println(redis.ping());
            }
            case "help" -> {
                out.println("Available commands: " + String.join(", ", COMMANDS));
            }
            case "exit", "quit" -> {
                out.println("Bye!");
                return false;
            }
            default -> out.println("Unknown command: " + cmd + ". Type 'help' for available commands.");
        }
        return true;
    }
}
