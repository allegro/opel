package pl.allegro.tech.opel;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

class SumOperatorExpressionNode extends BinaryOperationExpressionNode {

    private final ImplicitConversion implicitConversion;

    public SumOperatorExpressionNode(ExpressionNode left, ExpressionNode right, ImplicitConversion implicitConversion) {
        super(left, right);
        this.implicitConversion = implicitConversion;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        return left().getValue(context).thenCombine(right().getValue(context), (left, right) -> {
            if (left == null && right == null) {
                return BigDecimal.ZERO;
            }
            if (left == null && (right instanceof BigDecimal || right instanceof String)) {
                return right;
            }
            if (right == null && (left instanceof BigDecimal || left instanceof String)) {
                return left;
            }
            if (left == null && implicitConversion.hasConverter(right, BigDecimal.class)) {
                return implicitConversion.convert(right, BigDecimal.class);
            }
            if (left == null && implicitConversion.hasConverter(right, String.class)) {
                return implicitConversion.convert(right, String.class);
            }
            if (right == null && implicitConversion.hasConverter(left, BigDecimal.class)) {
                return implicitConversion.convert(left, BigDecimal.class);
            }
            if (right == null && implicitConversion.hasConverter(left, String.class)) {
                return implicitConversion.convert(left, String.class);
            }
            if (left instanceof BigDecimal && right instanceof BigDecimal) {
                return sumNumbers((BigDecimal) left, (BigDecimal) right);
            }
            if (left instanceof String && right instanceof String) {
                return sumStrings((String) left, (String) right);
            }
            if (left instanceof BigDecimal && implicitConversion.hasConverter(right, BigDecimal.class)) {
                return sumNumbers((BigDecimal) left, implicitConversion.convert(right, BigDecimal.class));
            }
            if (left instanceof String && implicitConversion.hasConverter(right, String.class)) {
                return sumStrings((String) left, implicitConversion.convert(right, String.class));
            }
            if (right instanceof BigDecimal && implicitConversion.hasConverter(left, BigDecimal.class)) {
                return sumNumbers(implicitConversion.convert(left, BigDecimal.class), (BigDecimal) right);
            }
            if (right instanceof String && implicitConversion.hasConverter(left, String.class)) {
                return sumStrings(implicitConversion.convert(left, String.class), (String) right);
            }
            if (implicitConversion.hasConverter(left, BigDecimal.class) && implicitConversion.hasConverter(right, BigDecimal.class)) {
                return sumNumbers(implicitConversion.convert(left, BigDecimal.class), implicitConversion.convert(right, BigDecimal.class));
            }
            if (implicitConversion.hasConverter(left, String.class) && implicitConversion.hasConverter(right, String.class)) {
                return sumStrings(implicitConversion.convert(left, String.class), implicitConversion.convert(right, String.class));
            }
            throw new OpelException("Can't sum " + Optional.ofNullable(left).map(it -> it.getClass().getSimpleName()).orElse("null") + " with " + Optional.ofNullable(right).map(it -> it.getClass().getSimpleName()).orElse("null"));
        });
    }

    private BigDecimal sumNumbers(BigDecimal left, BigDecimal right) {
        return left.add(right);
    }

    private String sumStrings(String left, String right) {
        return left + right;
    }
}
