package pl.allegro.tech.opel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

class IdentifierExpressionNode implements OpelNode {
    final String identifier;

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

    @Override
    public List<IdentifierExpressionNode> getRequiredIdentifiers() {
        return List.of(this);
    }

    @Override
    public void accept(OpelNodeVisitor visitor) {
        visitor.visit(this);
    }
}
