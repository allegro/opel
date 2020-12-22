package pl.allegro.tech.opel;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

class LogicalOperatorExpressionNode extends BinaryOperationExpressionNode {

    private final Operator logicalOperator;
    private final ImplicitConversion conversion;

    private LogicalOperatorExpressionNode(Operator logicalOperator, OpelNode left, OpelNode right, ImplicitConversion implicitConversion) {
        super(left, right);
        this.logicalOperator = logicalOperator;
        this.conversion = implicitConversion;
    }

    static LogicalOperatorExpressionNode andOperator(OpelNode left, OpelNode right, ImplicitConversion implicitConversion) {
        return new LogicalOperatorExpressionNode(Operator.AND, left, right, implicitConversion);
    }

    static LogicalOperatorExpressionNode orOperator(OpelNode left, OpelNode right, ImplicitConversion implicitConversion) {
        return new LogicalOperatorExpressionNode(Operator.OR, left, right, implicitConversion);
    }

    @Override
    public CompletableFuture<Boolean> getValue(EvalContext context) {
        return left().getValue(context).thenCompose(left -> wrappingExceptionsWithOpelException(left, () -> {
            if (logicalOperator == Operator.OR && Boolean.TRUE.equals(conversion.convert(left, Boolean.class))) {
                return CompletableFuture.completedFuture(true);
            } else if (logicalOperator == Operator.AND && Boolean.FALSE.equals(conversion.convert(left, Boolean.class))) {
                return CompletableFuture.completedFuture(false);
            } else {
                return right().getValue(context).thenApply(right -> wrappingExceptionsWithOpelException(left, right,
                        () -> Boolean.TRUE.equals(conversion.convert(right, Boolean.class))));
            }
        }));
    }

    private <T> T wrappingExceptionsWithOpelException(Object left, Supplier<T> wrappedBody) {
        try {
            return wrappedBody.get();
        } catch (Exception e) {
            throw new OpelLogicalExpressionException(logicalOperator, left, e);
        }
    }

    private <T> T wrappingExceptionsWithOpelException(Object left, Object right, Supplier<T> wrappedBody) {
        try {
            return wrappedBody.get();
        } catch (Exception e) {
            throw new OpelLogicalExpressionException(logicalOperator, left, right, e);
        }
    }
}
