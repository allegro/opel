package pl.allegro.tech.opel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

class FieldAccessExpressionNode implements OpelNode {
    final OpelNode subject;
    final OpelNode fieldName;

    public FieldAccessExpressionNode(OpelNode subject, OpelNode fieldName) {
        this.subject = subject;
        this.fieldName = fieldName;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        return extractValueFromMap(subject.getValue(context), fieldName.getValue(context));
    }

    private CompletableFuture<?> extractValueFromMap(CompletableFuture<?> obj, CompletableFuture<?> key) {
        return obj.thenCombine(key, (it, k) -> {
            if (it == null) {
                return null;
            }
            if (it instanceof Map) {
                return ((Map) it).get(k);
            }
            throw new OpelException("Give me a map, given " + it.getClass().getSimpleName());
        });
    }

    @Override
    public List<IdentifierExpressionNode> getRequiredIdentifiers() {
        return subject.getRequiredIdentifiers();
    }

    @Override
    public void accept(OpelNodeVisitor visitor) {
        visitor.visit(this);
    }
}
