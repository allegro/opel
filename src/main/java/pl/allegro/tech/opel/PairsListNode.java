package pl.allegro.tech.opel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PairsListNode implements OpelNode {
    private final List<PairNode> pairs;

    public PairsListNode(List<PairNode> pairs) {
        this.pairs = pairs;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        throw new UnsupportedOperationException("Can't get value on PairsListNode");
    }

    public List<PairNode> getPairs() {
        return pairs;
    }
}
