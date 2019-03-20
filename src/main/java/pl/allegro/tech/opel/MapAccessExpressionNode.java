package pl.allegro.tech.opel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MapAccessExpressionNode implements OpelNode {
    private final OpelNode subject;
    private final OpelNode fieldName;

    public MapAccessExpressionNode(OpelNode subject, OpelNode fieldName) {
        this.subject = subject;
        this.fieldName = fieldName;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        return extractValueFromListOrMap(subject.getValue(context), fieldName.getValue(context));
    }

    private CompletableFuture<?> extractValueFromListOrMap(CompletableFuture<?> obj, CompletableFuture<?> key) {
        return obj.thenCombine(key, (it, k) -> {
            if (it == null) {
                return null;
            }
            if (it instanceof Map) {
                return ((Map) it).get(k);
            }
            if (it instanceof List && k instanceof BigDecimal) {
                try {
                    return ((List) it).get(((BigDecimal) k).intValueExact());
                } catch (IndexOutOfBoundsException e) {
                    throw new OpelException("List index out of bounds, given " + ((BigDecimal) k).intValueExact());
                }
            }
            if (it instanceof List && k != null) {
                throw new OpelException("List index needs to be a number, given " + k.getClass().getSimpleName());
            }
            throw new OpelException("Give me a map or list, given " + it.getClass().getSimpleName());
        });
    }
}
