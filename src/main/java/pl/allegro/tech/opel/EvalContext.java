package pl.allegro.tech.opel;


import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface EvalContext {

    Optional<CompletableFuture<?>> getValue(String name);

    default boolean hasValue(String name) {
        return getValue(name).isPresent();
    }

    static EvalContext empty() {
        return EvalContextBuilder.fromMap(Collections.emptyMap());
    }
}
