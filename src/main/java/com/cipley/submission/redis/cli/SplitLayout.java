package com.cipley.submission.redis.cli;

import com.cipley.submission.redis.logging.UILogAppender;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.Display;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SplitLayout {
    private static final int LOG_ROWS = 6;
    private static final String DIVIDER_CHAR = "-";

    private final Terminal terminal;
    private final Display display;
    private final List<String> logLines = new ArrayList<>();

    // Main area content lines (set by the active screen)
    private List<AttributedString> mainLines = new ArrayList<>();

    public SplitLayout(Terminal terminal) {
        this.terminal = terminal;
        this.display = new Display(terminal, true);
    }

    /** Called by screens to push their rendered lines into the main area. */
    public void setMainContent(List<AttributedString> lines) {
        this.mainLines = lines;
    }

    /** Full redraw — call this after every user interaction. */
    public void render() {
        int totalRows = terminal.getRows();
        int mainRows  = totalRows - LOG_ROWS - 1; // -1 for the divider
        int width     = terminal.getColumns();

        List<AttributedString> rows = new ArrayList<>();

        // 1. Main area (top ~80%)
        for (int i = 0; i < mainRows; i++) {
            if (i < mainLines.size()) {
                rows.add(pad(mainLines.get(i), width));
            } else {
                rows.add(blank(width));
            }
        }

        // 2. Divider
        rows.add(divider(width));

        // 3. Log panel (bottom ~20%)
        drainNewLogs();
        int logStart = Math.max(0, logLines.size() - LOG_ROWS);
        for (int i = logStart; i < logLines.size(); i++) {
            rows.add(logLine(logLines.get(i), width));
        }
        // Pad remaining log rows if not enough lines yet
        while (rows.size() < totalRows) {
            rows.add(blank(width));
        }

        display.resize(totalRows, width);
        display.update(rows, 0);
    }

    private void drainNewLogs() {
        logLines.addAll(Arrays.asList(UILogAppender.drainLines()));
        // Keep only last 200 lines in memory
        if (logLines.size() > 200) {
            logLines.subList(0, logLines.size() - 200).clear();
        }
    }

    private AttributedString divider(int width) {
        AttributedStringBuilder b = new AttributedStringBuilder();
        b.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));
        b.append(" LOGS ");
        int remaining = width - 6;
        b.append(DIVIDER_CHAR.repeat(Math.max(0, remaining)));
        b.style(AttributedStyle.DEFAULT);
        return b.toAttributedString();
    }

    private AttributedString logLine(String text, int width) {
        AttributedStringBuilder b = new AttributedStringBuilder();
        b.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
        String truncated = text.length() > width ? text.substring(0, width) : text;
        b.append(truncated);
        b.style(AttributedStyle.DEFAULT);
        return pad(b.toAttributedString(), width);
    }

    private AttributedString pad(AttributedString s, int width) {
        if (s.length() >= width) return s;
        AttributedStringBuilder b = new AttributedStringBuilder();
        b.append(s);
        b.append(" ".repeat(width - s.length()));
        return b.toAttributedString();
    }

    private AttributedString blank(int width) {
        return new AttributedString(" ".repeat(width));
    }
}
