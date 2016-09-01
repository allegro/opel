package pl.allegro.opbox.opel;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

class MinusOperatorExpressionNode extends BinaryOperationExpressionNode {
    private final ImplicitConversion implicitConversion;

    public MinusOperatorExpressionNode(ExpressionNode left, ExpressionNode right, ImplicitConversion implicitConversion) {
        super(left, right);
        this.implicitConversion = implicitConversion;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        return left().getValue(context).thenCombine(right().getValue(context), (left, right) -> {
            if (left == null && right == null) {
                return BigDecimal.ZERO;
            }
            if (left == null && implicitConversion.hasConverter(right, BigDecimal.class)) {
                return implicitConversion.convert(right, BigDecimal.class).negate();
            }
            if (right == null && implicitConversion.hasConverter(left, BigDecimal.class)) {
                return implicitConversion.convert(left, BigDecimal.class);
            }
            if (implicitConversion.hasConverter(left, BigDecimal.class) && implicitConversion.hasConverter(right, BigDecimal.class)) {
                BigDecimal leftNumber = implicitConversion.convert(left, BigDecimal.class);
                BigDecimal rightNumber = implicitConversion.convert(right, BigDecimal.class);
                return leftNumber.subtract(rightNumber);
            }
            throw new OpelException("Can't subtract " + Optional.ofNullable(left).map(it -> it.getClass().getSimpleName()).orElse("null") + " with " + Optional.ofNullable(right).map(it -> it.getClass().getSimpleName()).orElse("null"));
        });
    }
}
