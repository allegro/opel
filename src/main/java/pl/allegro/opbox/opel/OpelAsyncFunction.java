package pl.allegro.opbox.opel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface OpelAsyncFunction<T> {

    CompletableFuture<T> apply(List<CompletableFuture<?>> args);

}
