package pl.allegro.tech.opel;

import java.util.ArrayList;
import java.util.List;

public class DeclarationsListStatementNode extends StatementNode {

    private final List<DeclarationStatementNode> declarations;

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
}
