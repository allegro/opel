package pl.allegro.tech.opel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

class LiteralExpressionNode implements OpelNode {
    private final Object value;

    public LiteralExpressionNode(Object value) {
        this.value = value;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext evalContext) {
        return CompletableFuture.completedFuture(value);
    }

    @Override
    public List<IdentifierExpressionNode> getRequiredIdentifiers() {
        return List.of();
    }
}
