package pl.allegro.tech.opel;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ListInstantiationExpressionNode implements OpelNode {
    private final ArgumentsListExpressionNode listElements;

    public ListInstantiationExpressionNode(ArgumentsListExpressionNode listElements) {
        this.listElements = listElements;
    }

    public ListInstantiationExpressionNode() {
        listElements = ArgumentsListExpressionNode.empty();
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        return FutureUtil.sequence(toListOfFutureOfObjects(listElements.getListOfValues(context)));
    }

    private List<CompletableFuture<Object>> toListOfFutureOfObjects(List<CompletableFuture<?>> list) {
        return list.stream().map(it -> it.thenApply(x -> (Object) x)).collect(Collectors.toList());
    }
}
