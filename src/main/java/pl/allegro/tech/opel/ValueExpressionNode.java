package pl.allegro.tech.opel;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

class ValueExpressionNode implements OpelNode {
    private final String valueName;

    public ValueExpressionNode(String valueName) {
        this.valueName = valueName;
    }

    public static ValueExpressionNode create(OpelNode node) {
        if (node instanceof IdentifierExpressionNode) {
            return new ValueExpressionNode(((IdentifierExpressionNode) node).getIdentifier());
        }
        throw new IllegalArgumentException("Cannot create ValueExpressionNode from " + node.getClass().getSimpleName());
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        Optional<CompletableFuture<?>> variable = context.getValue(valueName);
        return variable.orElseThrow(() -> new OpelException("Unknown value " + valueName));
    }
}
