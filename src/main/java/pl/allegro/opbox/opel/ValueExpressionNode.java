package pl.allegro.opbox.opel;

import java.util.concurrent.CompletableFuture;

class ValueExpressionNode implements ExpressionNode {
    private final Object value;

    public ValueExpressionNode(Object value) {
        this.value = value;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext evalContext) {
        return CompletableFuture.completedFuture(value);
    }
}
