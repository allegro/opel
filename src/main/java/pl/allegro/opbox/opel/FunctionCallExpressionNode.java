package pl.allegro.opbox.opel;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

class FunctionCallExpressionNode implements ExpressionNode {
    private final String functionName;
    private final Optional<ArgumentsListExpressionNode> arguments;

    public FunctionCallExpressionNode(String functionName, ArgumentsListExpressionNode arguments) {
        this.functionName = functionName;
        this.arguments = Optional.of(arguments);
    }

    private FunctionCallExpressionNode(String functionName) {
        this.functionName = functionName;
        this.arguments = Optional.empty();
    }

    static FunctionCallExpressionNode create(ExpressionNode identifier, ExpressionNode args) {
        if (identifier instanceof IdentifierExpressionNode && args instanceof ArgumentsListExpressionNode) {
            String identifierValue = ((IdentifierExpressionNode) identifier).getIdentifier();
            return new FunctionCallExpressionNode(identifierValue, (ArgumentsListExpressionNode) args);
        }
        throw new IllegalArgumentException("Cannot create FunctionCallExpressionNode from " + identifier.getClass().getSimpleName() + " and " + args.getClass().getSimpleName());
    }

    static FunctionCallExpressionNode create(ExpressionNode identifier) {
        if (identifier instanceof IdentifierExpressionNode) {
            String identifierValue = ((IdentifierExpressionNode) identifier).getIdentifier();
            return new FunctionCallExpressionNode(identifierValue);
        }
        throw new IllegalArgumentException("Cannot create FunctionCallExpressionNode from " + identifier.getClass().getSimpleName());
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        OpelAsyncFunction<?> function = context
                .getFunction(functionName)
                .orElseThrow(() -> new RuntimeException("Function " + functionName + " not found."));
        List<CompletableFuture<?>> args = arguments.map(ags -> ags.getListOfValues(context)).orElse(Collections.emptyList());
        return function.apply(args);
    }
}
