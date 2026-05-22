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
    private static final int LOG_ROWS = 15;
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

    /** Print directly to terminal, bypassing the layout. Used by simple prompt screens. */
    public void printDirect(String text) {
        terminal.writer().print(text);
        terminal.writer().flush();
    }

    /** Clear the entire screen. */
    public void clear() {
        terminal.puts(org.jline.utils.InfoCmp.Capability.clear_screen);
        terminal.writer().flush();
    }

    /** Full redraw — call this after every user interaction. */
    public void render() {
        int totalRows = terminal.getHeight();
        int mainRows  = Math.max(1, totalRows - LOG_ROWS - 1); // -1 for the divider
        int width     = Math.max(1, terminal.getWidth());

        // Clear screen before every render to prevent lines bleeding over
        clear();
        display.resize(totalRows, width);
        display.reset(); // force full redraw, no diffing against stale state

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

        // Move cursor to just after the main content, before the divider
        int promptRow = mainLines.size() + 1; // +1 for the blank padding
        terminal.writer().print(String.format("\033[%d;1H", promptRow));
        terminal.writer().flush();
    }

    public int getWidth() {
        return Math.max(1, terminal.getWidth());
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
