package pl.allegro.tech.opel;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

class ValueExpressionNode implements OpelNode {
    private final IdentifierExpressionNode node;

    public ValueExpressionNode(IdentifierExpressionNode node) {
        this.node = node;
    }

    public static ValueExpressionNode create(OpelNode node) {
        if (node instanceof IdentifierExpressionNode) {
            return new ValueExpressionNode(((IdentifierExpressionNode) node));
        }
        throw new IllegalArgumentException("Cannot create ValueExpressionNode from " + node.getClass().getSimpleName());
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        String identifier = node.getIdentifier();
        Optional<CompletableFuture<?>> variable = context.getValue(identifier);
        return variable.orElseThrow(() -> new OpelException("Unknown value " + identifier));
    }

    @Override
    public List<IdentifierExpressionNode> getRequiredIdentifiers() {
        return List.of(node);
    }
}
