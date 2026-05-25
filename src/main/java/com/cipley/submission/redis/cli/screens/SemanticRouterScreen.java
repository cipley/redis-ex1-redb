package com.cipley.submission.redis.cli.screens;

import com.cipley.submission.redis.cli.SplitLayout;
import com.cipley.submission.redis.config.SemanticRouterConfig;
import com.cipley.submission.redis.semantic.Routes;
import com.redis.vl.extensions.router.SemanticRouter;
import com.redis.vl.utils.vectorize.SentenceTransformersVectorizer;
import org.jline.utils.AttributedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.UnifiedJedis;

import java.util.ArrayList;

public class SemanticRouterScreen extends BaseScreen implements Screen {
    private static final Logger logger = LoggerFactory.getLogger(SemanticRouterScreen.class);

    private final UnifiedJedis jedis;
    private final SentenceTransformersVectorizer vectorizer;
    private final SemanticRouter router;
    private String statusMessage = "";

    public SemanticRouterScreen(SemanticRouterConfig config) {
        this.jedis = config.getUnifiedJedis();
        this.vectorizer = new SentenceTransformersVectorizer("sentence-transformers/all-mpnet-base-v2");

        this.router = SemanticRouter.builder()
                .name("topic-redis-exercise")
                .vectorizer(vectorizer)
                .routes(Routes.routes)
                .jedis(jedis)
                .overwrite(false)
                .build();
    }

    @Override
    public void render(SplitLayout layout) {
        var lines = new ArrayList<AttributedString>();
        lines.add(title("  Redis Exercise - 3) Semantic Router"));
        lines.add(blank());
        lines.add(blank());
        lines.add(option("  [0]  Back"));
        lines.add(blank());
        if (!statusMessage.isBlank()) {
            lines.addAll(wrap(statusMessage, layout.getWidth() - 4));
        }
        layout.setMainContent(lines);
    }

    @Override
    public Screen handleInput(String input, SplitLayout layout) {
        if (input.equals("0")) {
            return null;
        }
        routeMatch(input.trim());
        return this;
    }

    @Override
    public String prompt() {
        return "Enter your prompt: ";
    }

    private void routeMatch(String input) {
        var match = router.route(input);
        if (match != null && match.getName() != null) {
            statusMessage = match.getName();
        } else {
            statusMessage = "Match not found";
        }
    }
}
