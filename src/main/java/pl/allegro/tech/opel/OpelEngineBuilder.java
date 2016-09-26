package pl.allegro.tech.opel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class OpelEngineBuilder {
    private final Map<String, OpelAsyncFunction<?>> embeddedFunctions = new HashMap<>();
    private final Map<String, CompletableFuture<Object>> embeddedValues = new HashMap<>();
    private MethodExecutionFilter methodExecutionFilter = MethodExecutionFilters.ALLOW_ALL;
    private final ImplicitConversion implicitConversion;

    private OpelEngineBuilder() {
        implicitConversion = new ImplicitConversion();
        implicitConversion.registerNumberConversion();
    }

    public static OpelEngineBuilder create() {
        return new OpelEngineBuilder();
    }

    public OpelEngineBuilder withFunction(String functionName, OpelAsyncFunction<?> function) {
        embeddedFunctions.put(functionName, function);
        return this;
    }

    public OpelEngineBuilder withFunctions(Map<String, OpelAsyncFunction<?>> functions) {
        embeddedFunctions.putAll(functions);
        return this;
    }

    public OpelEngineBuilder withValue(String valueName, CompletableFuture<Object> value) {
        embeddedValues.put(valueName, value);
        return this;
    }

    public OpelEngineBuilder withValues(Map<String, CompletableFuture<Object>> values) {
        embeddedValues.putAll(values);
        return this;
    }

    public OpelEngineBuilder withCompletedValue(String valueName, Object value) {
        embeddedValues.put(valueName, CompletableFuture.completedFuture(value));
        return this;
    }

    public OpelEngineBuilder withMethodExecutionFilter(MethodExecutionFilter methodExecutionFilter) {
        this.methodExecutionFilter = methodExecutionFilter;
        return this;
    }

    public <T, R> OpelEngineBuilder withImplicitConversion(Class<T> from, Class<R> to, Function<T, R> conversion) {
        implicitConversion.register(new ImplicitConversionUnit<>(from, to, conversion));
        return this;
    }

    public OpelEngine build() {
        EvalContext context = EvalContextBuilder.create()
                .withFunctions(embeddedFunctions)
                .withValues(embeddedValues)
                .build();
        return new OpelEngine(methodExecutionFilter, implicitConversion, context);
    }
}
