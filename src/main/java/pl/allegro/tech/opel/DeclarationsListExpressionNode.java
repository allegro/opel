package pl.allegro.tech.opel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DeclarationsListExpressionNode implements ExpressionNode {

    private final List<DeclarationExpressionNode> declarations;

    public DeclarationsListExpressionNode(List<DeclarationExpressionNode> declarations, DeclarationExpressionNode declaration) {
        this.declarations = new ArrayList<>(declarations);
        this.declarations.add(declaration);
    }
    public DeclarationsListExpressionNode(List<DeclarationExpressionNode> declarations) {
        this.declarations = declarations;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        throw new UnsupportedOperationException("Can't get value on DeclarationsListExpressionNode");
    }

    public List<DeclarationExpressionNode> getDeclarations() {
        return declarations;
    }

    public boolean isEmpty() {
        return declarations.isEmpty();
    }
}
