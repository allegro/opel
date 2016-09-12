package pl.allegro.tech.opel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class OpelEngineBuilder {
    private final Map<String, OpelAsyncFunction<?>> embeddedFunctions = new HashMap<>();
    private final Map<String, CompletableFuture<Object>> embeddedVariables = new HashMap<>();
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

    public OpelEngineBuilder withVariable(String variableName, CompletableFuture<Object> value) {
        embeddedVariables.put(variableName, value);
        return this;
    }

    public OpelEngineBuilder withVariables(Map<String, CompletableFuture<Object>> variables) {
        embeddedVariables.putAll(variables);
        return this;
    }

    public OpelEngineBuilder withCompletedVariable(String variableName, Object value) {
        embeddedVariables.put(variableName, CompletableFuture.completedFuture(value));
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
                .withVariables(embeddedVariables)
                .build();
        return new OpelEngine(methodExecutionFilter, implicitConversion, context);
    }
}
