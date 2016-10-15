package pl.allegro.tech.opel;


import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface EvalContext {

    Optional<CompletableFuture<?>> getValue(String name);

    static EvalContext empty() {
        return EvalContextBuilder.fromMaps(Collections.emptyMap());
    }
}
