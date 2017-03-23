package pl.allegro.tech.opel;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AnonymousFunctionExpressionNode implements OpelNode {

    private final OpelNode expression;
    private final ArgumentsListExpressionNode arguments;

    public AnonymousFunctionExpressionNode(OpelNode expression, ArgumentsListExpressionNode arguments) {
        this.expression = expression;
        this.arguments = arguments;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        return expression.getValue(context).thenCompose(function -> {
            if (function instanceof OpelAsyncFunction) {
                return ((OpelAsyncFunction<?>) function).apply(arguments.getArgs().stream().map(it -> it.getValue(context)).collect(Collectors.toList()));
            }
            throw new OpelException("Can't use expression of type " + expression.getClass().getSimpleName() + " as a function");
        });
    }
}
