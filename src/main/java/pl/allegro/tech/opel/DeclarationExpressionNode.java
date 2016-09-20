package pl.allegro.tech.opel;

import java.util.concurrent.CompletableFuture;

public class DeclarationExpressionNode implements ExpressionNode {
    private final IdentifierExpressionNode identifier;
    private final ExpressionNode expression;

    public DeclarationExpressionNode(ExpressionNode identifier, ExpressionNode expression) {
        this.identifier = (IdentifierExpressionNode) identifier;
        this.expression = expression;
    }

    public IdentifierExpressionNode getIdentifier() {
        return identifier;
    }

    public ExpressionNode getExpression() {
        return expression;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        throw new UnsupportedOperationException("Can't get value on DeclarationExpressionNode");
    }
}
