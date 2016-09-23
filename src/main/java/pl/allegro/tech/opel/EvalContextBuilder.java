package pl.allegro.tech.opel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class EvalContextBuilder {
    private final Map<String, CompletableFuture<Object>> variables = new HashMap<>();
    private final Map<String, OpelAsyncFunction<?>> functions = new HashMap<>();
    private Optional<EvalContext> externalEvalContext = Optional.empty();

    static EvalContext fromMaps(Map<String, CompletableFuture<Object>> variables, Map<String, OpelAsyncFunction<?>> functions) {
        return new EvalContext() {
            @Override
            public Optional<OpelAsyncFunction<?>> getFunction(String name) {
                return Optional.ofNullable(functions.get(name));
            }

            @Override
            public Optional<CompletableFuture<?>> getVariable(String name) {
                return Optional.ofNullable(variables.get(name));
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

    public EvalContextBuilder withVariable(String variableName, CompletableFuture<Object> variable) {
        variables.put(variableName, variable);
        return this;
    }

    public EvalContextBuilder withVariables(Map<String, CompletableFuture<Object>> variables) {
        this.variables.putAll(variables);
        return this;
    }

    public EvalContextBuilder withCompletedVariable(String variableName, Object variable) {
        variables.put(variableName, CompletableFuture.completedFuture(variable));
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

    public boolean hasVariable(String varName) {
        return variables.containsKey(varName);
    }

    public boolean hasFunction(String funName) {
        return functions.containsKey(funName);
    }

    public EvalContext build() {
        return externalEvalContext.map(external -> mergeContexts(fromMaps(variables, functions), external))
                .orElseGet(() -> fromMaps(variables, functions));
    }

    static EvalContext mergeContexts(EvalContext primary, EvalContext secondary) {
        return new EvalContext() {
            @Override
            public Optional<OpelAsyncFunction<?>> getFunction(String name) {
                Optional<OpelAsyncFunction<?>> function = primary.getFunction(name);
                return function.isPresent() ? function : secondary.getFunction(name);
            }

            @Override
            public Optional<CompletableFuture<?>> getVariable(String name) {
                Optional<CompletableFuture<?>> variable = primary.getVariable(name);
                return (variable.isPresent()) ? variable : secondary.getVariable(name);
            }
        };
    }
}
