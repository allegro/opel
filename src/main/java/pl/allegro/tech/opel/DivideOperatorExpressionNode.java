package pl.allegro.tech.opel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CompletableFuture;

class DivideOperatorExpressionNode extends BinaryOperationExpressionNode {
    private final ImplicitConversion implicitConversion;

    public DivideOperatorExpressionNode(OpelNode left, OpelNode right, ImplicitConversion implicitConversion) {
        super(left, right);
        this.implicitConversion = implicitConversion;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        return left().getValue(context).thenCombine(right().getValue(context), (left, right) -> {
            if (right == null) {
                throw new OpelException("Can't divide " + left.getClass().getSimpleName() + " by null");
            }
            if (left == null) {
                return BigDecimal.ZERO;
            }
            if (implicitConversion.hasConverter(left, BigDecimal.class) && implicitConversion.hasConverter(right, BigDecimal.class)) {
                BigDecimal leftNumber = implicitConversion.convert(left, BigDecimal.class);
                BigDecimal rightNumber = implicitConversion.convert(right, BigDecimal.class);
                return leftNumber.divide(rightNumber, 10, RoundingMode.HALF_DOWN);
            }
            throw new OpelException("Can't divide " + left.getClass().getSimpleName() + " by " + right.getClass().getSimpleName());
        });
    }
}
