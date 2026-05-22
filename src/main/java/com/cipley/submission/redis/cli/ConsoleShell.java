package com.cipley.submission.redis.cli;

import com.cipley.submission.redis.cli.screens.ConnectionScreen;
import com.cipley.submission.redis.cli.screens.Screen;
import com.cipley.submission.redis.config.ApplicationConfig;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ConsoleShell {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleShell.class);

    private final Terminal terminal;
    private final LineReader reader;
    private final SplitLayout layout;
    private Screen currentScreen;

    public ConsoleShell(ApplicationConfig applicationConfig) throws IOException {
        this.terminal = TerminalBuilder.builder()
                .system(true)
                .build();
        this.reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .variable(LineReader.HISTORY_FILE, ".redis-dipo-submission-history")
                .build();

        this.layout = new SplitLayout(terminal);
        this.currentScreen = new ConnectionScreen(applicationConfig);
    }

    public void start() {
        logger.info("Shell started");

        // Clear screen once at startup
        layout.clear();

        while (currentScreen != null) {
            currentScreen.render(layout);
            if (currentScreen.usesLayout()) {
                layout.render();
            }

            try {
                String line = reader.readLine(currentScreen.prompt());
                if (line == null) line = "";
                currentScreen = currentScreen.handleInput(line, layout);

            } catch (UserInterruptException e) {
                // Ctrl+C — redraw
            } catch (EndOfFileException e) {
                logger.info("EOF, exiting");
                break;
            }
        }

        // Clear screen before every render to prevent lines bleeding over
        layout.clear();

        try {
            terminal.close();
        } catch (IOException e) {
            logger.warn("Terminal close error", e);
        }
        logger.info("Shell exited");
    }
}
