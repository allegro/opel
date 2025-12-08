package pl.allegro.tech.opel;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MapBasedEvalContext implements EvalContext {
    private final Map<String, CompletableFuture<?>> values;

    public MapBasedEvalContext(Map<String, CompletableFuture<?>> values) {
        this.values = values;
    }

    @Override
    public Optional<CompletableFuture<?>> getValue(String name) {
        return Optional.ofNullable(values.get(name));
    }

    @Override
    public boolean hasValue(String name) {
        return values.containsKey(name);
    }

    @Override
    public Set<String> getKeys() {
        return values.keySet();
    }
}
