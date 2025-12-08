package pl.allegro.tech.opel;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MergedEvalContext implements EvalContext {

    private final EvalContext primary;
    private final EvalContext secondary;

    public MergedEvalContext(EvalContext primary, EvalContext secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    @Override
    public Optional<CompletableFuture<?>> getValue(String name) {
        return primary.getValue(name).or(() ->
                secondary.getValue(name));
    }

    @Override
    public boolean hasValue(String name) {
        return primary.hasValue(name) || secondary.hasValue(name);
    }

    @Override
    public Set<String> getKeys() {
        var allKeys = new HashSet<String>();
        allKeys.addAll(primary.getKeys());
        allKeys.addAll(secondary.getKeys());
        return  allKeys;
    }
}
