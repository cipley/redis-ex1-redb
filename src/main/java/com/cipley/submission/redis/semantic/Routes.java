package com.cipley.submission.redis.semantic;

import com.redis.vl.extensions.router.Route;

import java.util.List;
import java.util.Map;

public class Routes {
    public static final List<Route> routes = List.of(
            Route.builder()
                    .name("Gen AI Programming Topics")
                    .references(List.of(
                            "what is the currently trending AI model on the market?",
                            "is there any alternative to Claude Code that is cheaper?",
                            "tell me about how to enhance programming experience with AI.",
                            "how to optimize token usage when programming with AI model?",
                            "is the generated code from AI reliable enough for production?"
                    ))
                    .metadata(Map.of(
                            "category", "tech",
                            "type", "gen ai",
                            "priority", "1"
                    ))
                    .distanceThreshold(0.71)
                    .build(),
            Route.builder()
                    .name("Science Fiction Entertainment")
                    .references(List.of(
                            "is The Expanse series still ongoing?",
                            "tell me the synopsis of Start Trek SG9.",
                            "who is the current USS Enterprise captain?",
                            "what character does Ryan Gosling play as in Blade Runner 2099?"
                    ))
                    .metadata(Map.of(
                            "category", "entertainment",
                            "type", "science fiction",
                            "priority", "2"
                    ))
                    .distanceThreshold(0.72)
                    .build(),
            Route.builder()
                    .name("Classical Music")
                    .references(List.of(
                            "explain the history of Beethoven's life.",
                            "how does Mozart compose his musical score?",
                            "how difficult is it to play Chopin?",
                            "is there any etiquette when watching a classical music?"
                    ))
                    .metadata(Map.of(
                            "category", "art",
                            "type", "classical",
                            "priority", "3"
                    ))
                    .distanceThreshold(0.73)
                    .build()
    );
}
