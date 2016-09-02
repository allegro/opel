package pl.allegro.tech.opel;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

class MultiplyOperatorExpressionNode extends BinaryOperationExpressionNode {
    private final ImplicitConversion implicitConversion;

    public MultiplyOperatorExpressionNode(ExpressionNode left, ExpressionNode right, ImplicitConversion implicitConversion) {
        super(left, right);
        this.implicitConversion = implicitConversion;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        return left().getValue(context).thenCombine(right().getValue(context), (left, right) -> {
            if (left == null || right == null) {
                return BigDecimal.ZERO;
            }
            if (implicitConversion.hasConverter(left, BigDecimal.class) && implicitConversion.hasConverter(right, BigDecimal.class)) {
                BigDecimal leftNumber = implicitConversion.convert(left, BigDecimal.class);
                BigDecimal rightNumber = implicitConversion.convert(right, BigDecimal.class);
                return leftNumber.multiply(rightNumber);
            }
            throw new OpelException("Can't multiply " + left.getClass().getSimpleName() + " with " + right.getClass().getSimpleName());
        });
    }
}
