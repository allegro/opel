package pl.allegro.tech.opel;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ArgsGroupNode implements OpelNode {
    private final List<ArgumentsListExpressionNode> argsGroup;

    public ArgsGroupNode(List<ArgumentsListExpressionNode> argsGroup) {
        this.argsGroup = argsGroup;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        return null;
    }

    public static OpelNode empty() {
        return new ArgsGroupNode(Collections.emptyList());
    }

    public List<ArgumentsListExpressionNode> getGroups() {
        return argsGroup;
    }
}
