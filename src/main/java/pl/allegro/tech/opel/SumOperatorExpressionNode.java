package pl.allegro.tech.opel;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

class SumOperatorExpressionNode extends BinaryOperationExpressionNode {

    private final ImplicitConversion implicitConversion;

    public SumOperatorExpressionNode(OpelNode left, OpelNode right, ImplicitConversion implicitConversion) {
        super(left, right);
        this.implicitConversion = implicitConversion;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        return left().getValue(context).thenCombine(right().getValue(context), (left, right) -> {
            if (left == null) {
                return nullPlus(right);
            }
            if (right == null) {
                return nullPlus(left);
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
            throw new OpelException("Can't sum " + left.getClass().getSimpleName() + " with " + right.getClass().getSimpleName());
        });
    }

    private Object nullPlus(Object node) {
        if (node == null) {
            return BigDecimal.ZERO;
        }
        if (node instanceof BigDecimal || node instanceof String) {
            return node;
        }
        if (implicitConversion.hasConverter(node, BigDecimal.class)) {
            return implicitConversion.convert(node, BigDecimal.class);
        }
        if (implicitConversion.hasConverter(node, String.class)) {
            return implicitConversion.convert(node, String.class);
        }
        throw new OpelException("Can't sum null with " + node.getClass().getSimpleName());
    }

    private BigDecimal sumNumbers(BigDecimal left, BigDecimal right) {
        return left.add(right);
    }

    private String sumStrings(String left, String right) {
        return left + right;
    }
}
