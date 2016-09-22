package pl.allegro.tech.opel;

import java.util.concurrent.CompletableFuture;

class CompareOperatorExpressionNode extends BinaryOperationExpressionNode {
    private final boolean greater;
    private final boolean equal;
    private final ImplicitConversion implicitConversion;

    private CompareOperatorExpressionNode(boolean greater, boolean equal, OpelNode left, OpelNode right, ImplicitConversion implicitConversion) {
        super(left, right);
        this.greater = greater;
        this.equal = equal;
        this.implicitConversion = implicitConversion;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        return left().getValue(context).thenCombine(right().getValue(context), (left, right) -> {
            if (left == null && right == null) {
                return equal;
            }
            if (left == null) {
                return !greater;
            }
            if (right == null) {
                return greater;
            }
            if (left instanceof Comparable && implicitConversion.hasConverter(right, left.getClass())) {
                int comparison = ((Comparable) left).compareTo(implicitConversion.convert(right, left.getClass()));
                return (comparison < 0 && !greater) || (comparison == 0 && equal) || (comparison > 0 && greater);
            }
            if (right instanceof Comparable && implicitConversion.hasConverter(left, right.getClass())) {
                int comparison = ((Comparable) implicitConversion.convert(left, right.getClass())).compareTo(right);
                return (comparison < 0 && !greater) || (comparison == 0 && equal) || (comparison > 0 && greater);
            }
            throw new OpelException("Can't compare " + left.getClass().getSimpleName() + " with " + right.getClass().getSimpleName());
        });
    }

    public static OpelNode greaterThen(OpelNode left, OpelNode right, ImplicitConversion implicitConversion) {
        return new CompareOperatorExpressionNode(true, false, left, right, implicitConversion);
    }

    public static OpelNode greaterOrEqual(OpelNode left, OpelNode right, ImplicitConversion implicitConversion) {
        return new CompareOperatorExpressionNode(true, true, left, right, implicitConversion);
    }

    public static OpelNode lowerThen(OpelNode left, OpelNode right, ImplicitConversion implicitConversion) {
        return new CompareOperatorExpressionNode(false, false, left, right, implicitConversion);
    }

    public static OpelNode lowerOrEqual(OpelNode left, OpelNode right, ImplicitConversion implicitConversion) {
        return new CompareOperatorExpressionNode(false, true, left, right, implicitConversion);
    }
}
