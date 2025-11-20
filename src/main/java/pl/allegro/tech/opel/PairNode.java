package pl.allegro.tech.opel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PairNode implements OpelNode {
    private final OpelNode key;
    private final OpelNode value;

    public PairNode(OpelNode key, OpelNode value) {
        this.key = key;
        this.value = value;
    }

    public OpelNode getKey() {
        return key;
    }

    public OpelNode getValue() {
        return value;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
         return value.getValue(context);
    }

    @Override
    public List<IdentifierExpressionNode> getRequiredIdentifiers() {
        return CollectionUtil.getIdentifiers(List.of(key, value));
    }
}
