package pl.allegro.tech.opel;

import java.util.concurrent.CompletableFuture;

class IdentifierExpressionNode implements OpelNode {
    private final String identifier;

    public IdentifierExpressionNode(String identifier) {
        this.identifier = identifier;
    }

    String getIdentifier() {
        return identifier;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        return CompletableFuture.completedFuture(identifier);
    }
}
