package pl.allegro.tech.opel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

class EqualOperatorExpressionNode extends BinaryOperationExpressionNode {

    private static final Logger logger = LoggerFactory.getLogger(EqualOperatorExpressionNode.class);
    private final boolean equal;
    private final ImplicitConversion implicitConversion;

    private EqualOperatorExpressionNode(boolean equal, OpelNode left, OpelNode right, ImplicitConversion implicitConversion) {
        super(left, right);
        this.equal = equal;
        this.implicitConversion = implicitConversion;
    }

    public static EqualOperatorExpressionNode equalityOperator(OpelNode left, OpelNode right, ImplicitConversion implicitConversion) {
        return new EqualOperatorExpressionNode(true, left, right, implicitConversion);
    }

    public static EqualOperatorExpressionNode inequalityOperator(OpelNode left, OpelNode right, ImplicitConversion implicitConversion) {
        return new EqualOperatorExpressionNode(false, left, right, implicitConversion);
    }

    @Override
    public CompletableFuture<Boolean> getValue(EvalContext context) {
        return left().getValue(context).thenCombine(right().getValue(context), (left, right) -> {
            try {
                if (left == null && right == null) {
                    return equal;
                }
                if (left == null || right == null) {
                    return !equal;
                }
                if (implicitConversion.hasConverter(right, left.getClass())) {
                    return implicitConversion.convert(right, left.getClass()).equals(left) == equal;
                }
                if (implicitConversion.hasConverter(left, right.getClass())) {
                    return implicitConversion.convert(left, right.getClass()).equals(right) == equal;
                }
                return left.equals(right) == equal;
            } catch (Exception e) {
                logger.info("Error on comparing " + left + " with " + right, e);
                return false;
            }
        });
    }
}
