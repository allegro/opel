package pl.allegro.opbox.opel;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

class VariableExpressionNode implements ExpressionNode {
    private final String variableName;

    public VariableExpressionNode(String variableName) {
        this.variableName = variableName;
    }

    public static VariableExpressionNode create(ExpressionNode node) {
        if (node instanceof IdentifierExpressionNode) {
            return new VariableExpressionNode(((IdentifierExpressionNode) node).getIdentifier());
        }
        throw new IllegalArgumentException("Cannot create VariableExpressionNode from " + node.getClass().getSimpleName());
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        Optional<CompletableFuture<?>> variable = context.getVariable(variableName);
        return variable.orElseThrow(() -> new RuntimeException("Unknown variable " + variableName));
    }
}
