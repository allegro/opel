package pl.allegro.tech.opel;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

class ArgumentsListExpressionNode implements OpelNode {

    private final List<OpelNode> args;

    ArgumentsListExpressionNode(List<OpelNode> args) {
        this.args = args;
    }

    List<CompletableFuture<?>> getListOfValues(EvalContext context) {
        return args.stream().map(it -> it.getValue(context)).collect(Collectors.toList());
    }

    List<OpelNode> getArgs() {
        return args;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        throw new UnsupportedOperationException("Can't get value on ArgumentsListExpressionNode");
    }

    static ArgumentsListExpressionNode empty() {
        return new ArgumentsListExpressionNode(List.of());
    }

    @Override
    public List<IdentifierExpressionNode> getRequiredIdentifiers() {
        return CollectionUtil.getIdentifiers(args);
    }
}
