package pl.allegro.tech.opel;

import java.util.List;

public class DeclarationStatementNode extends StatementNode {
    final IdentifierExpressionNode identifier;
    final OpelNode expression;

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

    @Override
    public void accept(OpelNodeVisitor visitor) {
        visitor.visit(this);
    }
}
