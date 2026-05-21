package com.cipley.submission.redis.logging;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class UILogAppender extends AppenderBase<ILoggingEvent> {
    private static final int CAPACITY = 200;
    private static final BlockingQueue<String> BUFFER = new ArrayBlockingQueue<>(CAPACITY);

    private PatternLayoutEncoder encoder;

    public void setEncoder(PatternLayoutEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public void start() {
        if (encoder != null) {
            encoder.setContext(context);
            encoder.start();
        }
        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        var line = encoder != null
                ? new String(encoder.encode(event)).trim()
                : event.getFormattedMessage();
        // Drop oldest if full
        if (!BUFFER.offer(line)) {
            BUFFER.poll();
            BUFFER.offer(line);
        }
    }

    /** Drain all pending log lines. Called by the UI render loop. */
    public static String[] drainLines() {
        return BUFFER.toArray(new String[0]);
    }
}
