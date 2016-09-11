package pl.allegro.tech.opel;

import java.util.concurrent.CompletableFuture;

interface ExpressionNode {
    CompletableFuture<?> getValue(EvalContext context);
}
