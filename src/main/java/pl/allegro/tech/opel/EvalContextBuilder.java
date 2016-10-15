package pl.allegro.tech.opel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class EvalContextBuilder {
    private final Map<String, CompletableFuture<?>> values = new HashMap<>();
    private Optional<EvalContext> externalEvalContext = Optional.empty();

    static EvalContext fromMaps(Map<String, CompletableFuture<?>> values) {
        Map<String, CompletableFuture<?>> copiedValues = new HashMap<>(values);
        return name -> Optional.ofNullable(copiedValues.get(name));
    }

    public static EvalContextBuilder create() {
        return new EvalContextBuilder();
    }

    public EvalContextBuilder withExternalEvalContext(EvalContext evalContext) {
        this.externalEvalContext = Optional.of(evalContext);
        return this;
    }

    public EvalContextBuilder withValue(String valueName, CompletableFuture<Object> value) {
        values.put(valueName, value);
        return this;
    }

    public EvalContextBuilder withValues(Map<String, CompletableFuture<?>> values) {
        this.values.putAll(values);
        return this;
    }

    public EvalContextBuilder withCompletedValue(String valueName, Object value) {
        values.put(valueName, CompletableFuture.completedFuture(value));
        return this;
    }

    /**
     * use withCompletedValue method
     */
    @Deprecated
    public EvalContextBuilder withFunction(String functionName, OpelAsyncFunction<?> function) {
        return withCompletedValue(functionName, function);
    }

    /**
     * use withValues method
     */
    @Deprecated
    public EvalContextBuilder withFunctions(Map<String, OpelAsyncFunction<?>> functions) {
        functions.forEach(this::withCompletedValue);
        return this;
    }

    public boolean hasValue(String valueName) {
        return values.containsKey(valueName);
    }

    public EvalContext build() {
        return externalEvalContext.map(external -> mergeContexts(fromMaps(values), external))
                .orElseGet(() -> fromMaps(values));
    }

    static EvalContext mergeContexts(EvalContext primary, EvalContext secondary) {
        return name -> {
            Optional<CompletableFuture<?>> value = primary.getValue(name);
            return (value.isPresent()) ? value : secondary.getValue(name);
        };
    }
}
