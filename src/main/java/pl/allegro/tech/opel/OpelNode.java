package pl.allegro.tech.opel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

interface OpelNode {
    CompletableFuture<?> getValue(EvalContext context);
    List<IdentifierExpressionNode> getRequiredIdentifiers();
    default List<IdentifierExpressionNode> getDeclaredIdentifiers() {
        return List.of();
    }
    void accept(OpelNodeVisitor visitor);
}
