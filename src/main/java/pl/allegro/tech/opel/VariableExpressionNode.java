package pl.allegro.tech.opel;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

class VariableExpressionNode implements OpelNode {
    private final String variableName;

    public VariableExpressionNode(String variableName) {
        this.variableName = variableName;
    }

    public static VariableExpressionNode create(OpelNode node) {
        if (node instanceof IdentifierExpressionNode) {
            return new VariableExpressionNode(((IdentifierExpressionNode) node).getIdentifier());
        }
        throw new IllegalArgumentException("Cannot create VariableExpressionNode from " + node.getClass().getSimpleName());
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        Optional<CompletableFuture<?>> variable = context.getVariable(variableName);
        return variable.orElseThrow(() -> new OpelException("Unknown variable " + variableName));
    }
}
