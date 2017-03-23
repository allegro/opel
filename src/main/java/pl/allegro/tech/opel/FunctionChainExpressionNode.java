package pl.allegro.tech.opel;

import java.util.concurrent.CompletableFuture;

public class FunctionChainExpressionNode implements OpelNode {
    private final OpelNode expression;
    private final ArgsGroupNode argsGroups;

    public FunctionChainExpressionNode(OpelNode expression, ArgsGroupNode argsGroups) {
        this.expression = expression;
        this.argsGroups = argsGroups;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        CompletableFuture<?> result = expression.getValue(context);
        for (ArgumentsListExpressionNode argsGroup : argsGroups.getGroups()) {
            result = callFunction(result, argsGroup, context);
        }
        return result;
    }

    private CompletableFuture<Object> callFunction(CompletableFuture<?> function, ArgumentsListExpressionNode argsGroup, EvalContext context) {
        return function.thenCompose(fun -> {
            if (fun instanceof OpelAsyncFunction) {
                return ((OpelAsyncFunction) fun).apply(argsGroup.getListOfValues(context));
            }
            throw new OpelException("Can't use '" + fun.getClass().getSimpleName() + "' as a function");
        });
    }
}
