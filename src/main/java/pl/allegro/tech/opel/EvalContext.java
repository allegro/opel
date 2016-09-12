package pl.allegro.tech.opel;


import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface EvalContext {

    Optional<OpelAsyncFunction<?>> getFunction(String name);

    Optional<CompletableFuture<?>> getVariable(String name);

    static EvalContext empty() {
        return EvalContextBuilder.fromMaps(Collections.emptyMap(), Collections.emptyMap());
    }
}
