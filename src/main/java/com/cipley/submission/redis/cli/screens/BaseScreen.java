package com.cipley.submission.redis.cli.screens;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseScreen {
    // --- Styling helpers ---
    protected AttributedString title(String text) {
        return new AttributedStringBuilder()
                .style(AttributedStyle.BOLD.foreground(AttributedStyle.CYAN))
                .append(text)
                .style(AttributedStyle.DEFAULT)
                .toAttributedString();
    }

    protected AttributedString option(String text) {
        return new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
                .append(text)
                .style(AttributedStyle.DEFAULT)
                .toAttributedString();
    }

    protected AttributedString status(String text) {
        return new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
                .append(text)
                .style(AttributedStyle.DEFAULT)
                .toAttributedString();
    }

    protected AttributedString info(String text) {
        return new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT)
                .append(text)
                .toAttributedString();
    }

    protected AttributedString hint(String text) {
        return new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT))
                .append(text)
                .toAttributedString();
    }

    protected AttributedString error(String text) {
        return new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
                .append(text)
                .style(AttributedStyle.DEFAULT)
                .toAttributedString();
    }

    protected List<AttributedString> wrap(String text, int maxWidth) {
        List<AttributedString> result = new ArrayList<>();
        while (text.length() > maxWidth) {
            result.add(status("  " + text.substring(0, maxWidth)));
            text = text.substring(maxWidth);
        }
        if (!text.isBlank()) result.add(status("  " + text));
        return result;
    }

    protected AttributedString blank() {
        return new AttributedString("");
    }
}
