package pl.allegro.tech.opel;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

class FunctionCallExpressionNode implements OpelNode {
    private final String functionName;
    private final Optional<ArgumentsListExpressionNode> arguments;

    public FunctionCallExpressionNode(String functionName, ArgumentsListExpressionNode arguments) {
        this.functionName = functionName;
        this.arguments = Optional.of(arguments);
    }

    static FunctionCallExpressionNode create(OpelNode identifier, OpelNode args) {
        if (identifier instanceof IdentifierExpressionNode && args instanceof ArgumentsListExpressionNode) {
            String identifierValue = ((IdentifierExpressionNode) identifier).getIdentifier();
            return new FunctionCallExpressionNode(identifierValue, (ArgumentsListExpressionNode) args);
        }
        throw new IllegalArgumentException("Cannot create FunctionCallExpressionNode from " + identifier.getClass().getSimpleName() + " and " + args.getClass().getSimpleName());
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        CompletableFuture<OpelAsyncFunction<?>> function = getFunctionFromValue(context);
        List<CompletableFuture<?>> args = arguments.map(ags -> ags.getListOfValues(context)).orElse(Collections.emptyList());
        return function.thenCompose(fun -> fun.apply(args));
    }

    private CompletableFuture<OpelAsyncFunction<?>> getFunctionFromValue(EvalContext context) {
        CompletableFuture<? extends OpelAsyncFunction<?>> opelAsyncFunctionCompletableFuture = context.getValue(functionName)
                .map(val -> val.thenApply(it -> getValueAsFunction(it)))
                .orElseThrow(() -> new OpelException("Function '" + functionName + "' not found"));
        return opelAsyncFunctionCompletableFuture.thenApply(Function.identity());
    }

    private OpelAsyncFunction<?> getValueAsFunction(Object it) {
        if (it instanceof OpelAsyncFunction) {
            return (OpelAsyncFunction<?>) it;
        } else {
            throw new OpelException("Value '" + functionName + "' is not a function");
        }
    }
}
