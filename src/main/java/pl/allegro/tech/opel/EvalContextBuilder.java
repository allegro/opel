package pl.allegro.tech.opel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class EvalContextBuilder {
    private final Map<String, CompletableFuture<Object>> values = new HashMap<>();
    private final Map<String, OpelAsyncFunction<?>> functions = new HashMap<>();
    private Optional<EvalContext> externalEvalContext = Optional.empty();

    static EvalContext fromMaps(Map<String, CompletableFuture<Object>> values, Map<String, OpelAsyncFunction<?>> functions) {
        Map<String, CompletableFuture<Object>> copiedValues = new HashMap<>(values);
        Map<String, OpelAsyncFunction<?>> copiedFunctions = new HashMap<>(functions);
        return new EvalContext() {
            @Override
            public Optional<OpelAsyncFunction<?>> getFunction(String name) {
                return Optional.ofNullable(copiedFunctions.get(name));
            }

            @Override
            public Optional<CompletableFuture<?>> getValue(String name) {
                return Optional.ofNullable(copiedValues.get(name));
            }
        };
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

    public EvalContextBuilder withValues(Map<String, CompletableFuture<Object>> values) {
        this.values.putAll(values);
        return this;
    }

    public EvalContextBuilder withCompletedValue(String valueName, Object value) {
        values.put(valueName, CompletableFuture.completedFuture(value));
        return this;
    }

    public EvalContextBuilder withFunction(String functionName, OpelAsyncFunction<?> function) {
        functions.put(functionName, function);
        return this;
    }

    public EvalContextBuilder withFunctions(Map<String, OpelAsyncFunction<?>> functions) {
        this.functions.putAll(functions);
        return this;
    }

    public boolean hasValue(String valueName) {
        return values.containsKey(valueName);
    }

    public boolean hasFunction(String funName) {
        return functions.containsKey(funName);
    }

    public EvalContext build() {
        return externalEvalContext.map(external -> mergeContexts(fromMaps(values, functions), external))
                .orElseGet(() -> fromMaps(values, functions));
    }

    static EvalContext mergeContexts(EvalContext primary, EvalContext secondary) {
        return new EvalContext() {
            @Override
            public Optional<OpelAsyncFunction<?>> getFunction(String name) {
                Optional<OpelAsyncFunction<?>> function = primary.getFunction(name);
                return function.isPresent() ? function : secondary.getFunction(name);
            }

            @Override
            public Optional<CompletableFuture<?>> getValue(String name) {
                Optional<CompletableFuture<?>> value = primary.getValue(name);
                return (value.isPresent()) ? value : secondary.getValue(name);
            }
        };
    }
}
