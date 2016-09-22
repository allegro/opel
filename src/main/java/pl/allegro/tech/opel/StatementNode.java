package pl.allegro.tech.opel;

import java.util.concurrent.CompletableFuture;

abstract class StatementNode implements OpelNode {
    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        throw new UnsupportedOperationException("Can't get value on " + getClass().getSimpleName());
    }
}
