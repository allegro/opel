package pl.allegro.tech.opel;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FunctionInstantiationExpressionNode implements OpelNode {
    private final IdentifiersListNode arguments;
    private final OpelNode body;

    public FunctionInstantiationExpressionNode(IdentifiersListNode arguments, OpelNode body) {
        this.arguments = arguments;
        this.body = body;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        return CompletableFuture.completedFuture(new OpelAsyncFunction<Object>() {
            @Override
            public CompletableFuture<Object> apply(List<CompletableFuture<?>> args) {

                List<CompletableFuture<Object>> collect = arguments.getIdentifiers().stream()
                        .map(it -> it.getValue(context))
                        .map(it -> javaGenericWorkaround(it))
                        .collect(Collectors.toList());
                CompletableFuture<List<Object>> argsNames = FutureUtil.sequence(collect);

                return argsNames.thenCompose(names -> {
                    EvalContextBuilder contextBuilder = EvalContextBuilder.create().withExternalEvalContext(context);
                    for (int i = 0; i < names.size(); i++) {
                        String name = (String) names.get(i);
                        if (args.size() == i) {
                            throw new OpelException("Missing argument '" + name + "' in function call");
                        }
                        CompletableFuture<Object> value = args.get(i).thenApply(Function.identity());
                        contextBuilder.withValue(name, value);
                    }
                    EvalContext localContext = contextBuilder.build();
                    return body.getValue(localContext).thenApply(Function.identity());
                });

            }
        });
    }

    private CompletableFuture<Object> javaGenericWorkaround(CompletableFuture<?> future) {
        return future.thenApply(Function.identity());
    }
}
