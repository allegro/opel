package pl.allegro.tech.opel;

import java.util.concurrent.CompletableFuture;

class ValueExpressionNode implements OpelNode {
    private final Object value;

    public ValueExpressionNode(Object value) {
        this.value = value;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext evalContext) {
        return CompletableFuture.completedFuture(value);
    }
}
