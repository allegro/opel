package pl.allegro.tech.opel;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListInstantiationExpressionNode implements OpelNode {
    private final Optional<ArgumentsListExpressionNode> listElements;

    public ListInstantiationExpressionNode(ArgumentsListExpressionNode listElements) {
        this.listElements = Optional.of(listElements);
    }

    public ListInstantiationExpressionNode() {
        listElements = Optional.empty();
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        return listElements
                .map(it -> it.getListOfValues(context))
                .map(this::toListOfFutureOfObjects)
                .map(FutureUtil::sequence)
                .orElseGet(() -> CompletableFuture.completedFuture(Collections.emptyList()));
    }

    private List<CompletableFuture<Object>> toListOfFutureOfObjects(List<CompletableFuture<?>> list) {
        return list.stream().map(it -> it.thenApply(x -> (Object) x)).collect(Collectors.toList());
    }
}
