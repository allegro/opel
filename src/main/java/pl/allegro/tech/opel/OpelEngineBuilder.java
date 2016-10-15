package pl.allegro.tech.opel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class OpelEngineBuilder {
    private final Map<String, CompletableFuture<?>> embeddedValues = new HashMap<>();
    private MethodExecutionFilter methodExecutionFilter = MethodExecutionFilters.ALLOW_ALL;
    private final ImplicitConversion implicitConversion;

    private OpelEngineBuilder() {
        implicitConversion = new ImplicitConversion();
        implicitConversion.registerNumberConversion();
    }

    public static OpelEngineBuilder create() {
        return new OpelEngineBuilder();
    }

    /**
     * use withValue method
     */
    @Deprecated
    public OpelEngineBuilder withFunction(String functionName, OpelAsyncFunction<?> function) {
        return withCompletedValue(functionName, function);
    }

    /**
     * use withValues method
     */
    @Deprecated
    public OpelEngineBuilder withFunctions(Map<String, OpelAsyncFunction<?>> functions) {
        functions.forEach((name, function) -> withCompletedValue(name, function));
        return this;
    }

    public OpelEngineBuilder withValue(String valueName, CompletableFuture<Object> value) {
        embeddedValues.put(valueName, value);
        return this;
    }

    public OpelEngineBuilder withValues(Map<String, CompletableFuture<?>> values) {
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
                .withValues(embeddedValues)
                .build();
        return new OpelEngine(methodExecutionFilter, implicitConversion, context);
    }
}
