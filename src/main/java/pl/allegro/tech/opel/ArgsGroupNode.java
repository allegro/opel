package pl.allegro.tech.opel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ArgsGroupNode implements OpelNode {
    final List<ArgumentsListExpressionNode> argsGroup;

    public ArgsGroupNode(List<ArgumentsListExpressionNode> argsGroup) {
        this.argsGroup = argsGroup;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        throw new UnsupportedOperationException("Can't get value on ArgsGroupNode");
    }

    public static OpelNode empty() {
        return new ArgsGroupNode(List.of());
    }

    public List<ArgumentsListExpressionNode> getGroups() {
        return argsGroup;
    }

    @Override
    public List<IdentifierExpressionNode> getRequiredIdentifiers() {
        return CollectionUtil.getIdentifiers(argsGroup);
    }

    @Override
    public void accept(OpelNodeVisitor visitor) {
        visitor.visit(this);
    }
}
