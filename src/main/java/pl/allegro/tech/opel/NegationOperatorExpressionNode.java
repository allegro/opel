package pl.allegro.tech.opel;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class NegationOperatorExpressionNode implements OpelNode {

    private final OpelNode value;
    private final ImplicitConversion implicitConversion;

    public NegationOperatorExpressionNode(OpelNode value, ImplicitConversion implicitConversion) {
        this.value = value;
        this.implicitConversion = implicitConversion;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        return value.getValue(context).thenApply(it -> {
            if (it == null) {
                return BigDecimal.ZERO;
            }
            if (it instanceof BigDecimal) {
                return ((BigDecimal) it).negate();
            }
            if (implicitConversion.hasConverter(it, BigDecimal.class)) {
                return implicitConversion.convert(it, BigDecimal.class).negate();
            }
            throw new OpelException("Can negate only number, given " + it.getClass().getSimpleName());
        });
    }

    @Override
    public List<IdentifierExpressionNode> getRequiredIdentifiers() {
        return value.getRequiredIdentifiers();
    }
}
