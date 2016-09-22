package pl.allegro.tech.opel;

import java.util.concurrent.CompletableFuture;

interface OpelNode {
    CompletableFuture<?> getValue(EvalContext context);
}
