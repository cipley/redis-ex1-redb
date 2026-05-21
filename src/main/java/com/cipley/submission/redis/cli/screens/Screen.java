package com.cipley.submission.redis.cli.screens;

import com.cipley.submission.redis.cli.SplitLayout;

public interface Screen {
    /** Render content into the layout's main area. */
    void render(SplitLayout layout);

    /** Handle a line of user input. Returns the next screen, or null to exit. */
    Screen handleInput(String input, SplitLayout layout);
}
