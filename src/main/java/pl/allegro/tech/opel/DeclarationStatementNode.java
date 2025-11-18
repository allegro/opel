package pl.allegro.tech.opel;

import java.util.List;

public class DeclarationStatementNode extends StatementNode {
    private final IdentifierExpressionNode identifier;
    private final OpelNode expression;

    public DeclarationStatementNode(OpelNode identifier, OpelNode expression) {
        this.identifier = (IdentifierExpressionNode) identifier;
        this.expression = expression;
    }

    public IdentifierExpressionNode getIdentifier() {
        return identifier;
    }

    public OpelNode getExpression() {
        return expression;
    }

    @Override
    public List<IdentifierExpressionNode> getRequiredIdentifiers() {
        return expression.getRequiredIdentifiers();
    }

    @Override
    public List<IdentifierExpressionNode> getDeclaredIdentifiers() {
        return List.of(identifier);
    }
}
