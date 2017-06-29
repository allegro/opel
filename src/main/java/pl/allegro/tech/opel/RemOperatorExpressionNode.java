package pl.allegro.tech.opel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CompletableFuture;

class RemOperatorExpressionNode extends BinaryOperationExpressionNode {
    private final ImplicitConversion implicitConversion;

    public RemOperatorExpressionNode(OpelNode left, OpelNode right, ImplicitConversion implicitConversion) {
        super(left, right);
        this.implicitConversion = implicitConversion;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        return left().getValue(context).thenCombine(right().getValue(context), (left, right) -> {
            if (right == null) {
                throw new OpelException("Can't compute remainder of " + left.getClass().getSimpleName() + " by null");
            }
            if (left == null) {
                return BigDecimal.ZERO;
            }
            if (implicitConversion.hasConverter(left, BigDecimal.class) && implicitConversion.hasConverter(right, BigDecimal.class)) {
                BigDecimal leftNumber = implicitConversion.convert(left, BigDecimal.class);
                BigDecimal rightNumber = implicitConversion.convert(right, BigDecimal.class);
                return leftNumber.remainder(rightNumber);
            }
            throw new OpelException("Can't computer remainder of " + left.getClass().getSimpleName() + " by " + right.getClass().getSimpleName());
        });
    }
}
