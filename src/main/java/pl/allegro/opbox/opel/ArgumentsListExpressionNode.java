package pl.allegro.opbox.opel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

class ArgumentsListExpressionNode implements ExpressionNode {

    private final ExpressionNode head;
    private final Optional<ArgumentsListExpressionNode> tail;

    public ArgumentsListExpressionNode(ExpressionNode head) {
        this.head = head;
        this.tail = Optional.empty();
    }

    public ArgumentsListExpressionNode(ExpressionNode head, ArgumentsListExpressionNode tail) {
        if (head instanceof ArgumentsListExpressionNode) {
            throw new IllegalArgumentException("Head can't be " + head.getClass().getSimpleName());
        }
        this.head = head;
        this.tail = Optional.of(tail);
    }

    List<CompletableFuture<?>> getListOfValues(EvalContext context) {
        List<CompletableFuture<?>> result = new ArrayList<>();
        tail.ifPresent(t -> result.addAll(t.getListOfValues(context)));
        result.add(head.getValue(context));
        return result;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        throw new UnsupportedOperationException("Can't get value on ArgumentsListExpressionNode");
    }
}
