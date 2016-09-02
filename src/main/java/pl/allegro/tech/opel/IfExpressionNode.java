package pl.allegro.tech.opel;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

class IfExpressionNode extends BinaryOperationExpressionNode {
    private final ExpressionNode condition;
    private final ImplicitConversion implicitConversion;

    public IfExpressionNode(ExpressionNode condition, ExpressionNode trueValue, ExpressionNode falseValue, ImplicitConversion implicitConversion) {
        super(trueValue, falseValue);
        this.condition = condition;
        this.implicitConversion = implicitConversion;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        return condition.getValue(context).thenCompose(conditionValue -> calculateExpression(conditionValue, context));
    }

    private CompletableFuture<?> calculateExpression(Object truthValue, EvalContext context) {
        if (truthValue == null) {
            return right().getValue(context);
        } else if (truthValue instanceof Boolean) {
            return decision((Boolean) truthValue, () -> left().getValue(context), () -> right().getValue(context));
        } else if (implicitConversion.hasConverter(truthValue, Boolean.class)) {
            return decision(implicitConversion.convert(truthValue, Boolean.class), () -> left().getValue(context), () -> right().getValue(context));
        } else {
            throw new OpelException("'" + truthValue.getClass().getSimpleName() + "' can't be use as if expression argument.");
        }
    }

    private CompletableFuture<?> decision(Boolean truthValue, Supplier<CompletableFuture<?>> left, Supplier<CompletableFuture<?>> right) {
        if (truthValue) {
            return left.get();
        } else {
            return right.get();
        }
    }
}
