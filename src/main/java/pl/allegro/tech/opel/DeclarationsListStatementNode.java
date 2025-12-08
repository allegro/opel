package pl.allegro.tech.opel;

import java.util.ArrayList;
import java.util.List;

public class DeclarationsListStatementNode extends StatementNode {

    final List<DeclarationStatementNode> declarations;

    public DeclarationsListStatementNode(List<DeclarationStatementNode> declarations, DeclarationStatementNode declaration) {
        this.declarations = new ArrayList<>(declarations);
        this.declarations.add(declaration);
    }
    public DeclarationsListStatementNode(List<DeclarationStatementNode> declarations) {
        this.declarations = declarations;
    }

    public List<DeclarationStatementNode> getDeclarations() {
        return declarations;
    }

    public boolean isEmpty() {
        return declarations.isEmpty();
    }

    @Override
    public List<IdentifierExpressionNode> getRequiredIdentifiers() {
        return CollectionUtil.getIdentifiers(declarations);
    }

    @Override
    public List<IdentifierExpressionNode> getDeclaredIdentifiers() {
        return declarations.stream().map(DeclarationStatementNode::getIdentifier).toList();
    }

    @Override
    public void accept(OpelNodeVisitor visitor) {
        visitor.visit(this);
    }
}
