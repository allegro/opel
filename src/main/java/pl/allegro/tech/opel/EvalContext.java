package pl.allegro.tech.opel;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface EvalContext {

    Optional<OpelAsyncFunction<?>> getFunction(String name);

    Optional<CompletableFuture<?>> getVariable(String name);

    static EvalContext empty() {
        return Builder.fromMaps(Collections.emptyMap(), Collections.emptyMap());
    }

    class Builder {
        private final Map<String, CompletableFuture<Object>> variables = new HashMap<>();
        private final Map<String, OpelAsyncFunction<?>> functions = new HashMap<>();
        private Optional<EvalContext> parentEvalContext = Optional.empty();

        private static EvalContext fromMaps(Map<String, CompletableFuture<Object>> variables, Map<String, OpelAsyncFunction<?>> functions) {
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

        public static Builder create() {
            return new Builder();
        }

        public Builder withParentEvalContext(EvalContext evalContext) {
            this.parentEvalContext = Optional.of(evalContext);
            return this;
        }

        public Builder withVariable(String variableName, CompletableFuture<Object> variable) {
            variables.put(variableName, variable);
            return this;
        }

        public Builder withVariables(Map<String, CompletableFuture<Object>> variables) {
            this.variables.putAll(variables);
            return this;
        }

        public Builder withCompletedVariable(String variableName, Object variable) {
            variables.put(variableName, CompletableFuture.completedFuture(variable));
            return this;
        }

        public Builder withFunction(String functionName, OpelAsyncFunction<?> function) {
            functions.put(functionName, function);
            return this;
        }

        public Builder withFunctions(Map<String, OpelAsyncFunction<?>> functions) {
            this.functions.putAll(functions);
            return this;
        }

        public EvalContext build() {
            return parentEvalContext.map(this::mergeContexts).orElse(fromMaps(variables, functions));
        }

        EvalContext mergeContexts(EvalContext parent) {
            return new EvalContext() {
                @Override
                public Optional<OpelAsyncFunction<?>> getFunction(String name) {
                    if (functions.containsKey(name)) {
                        return Optional.ofNullable(functions.get(name));
                    }
                    return parent.getFunction(name);
                }

                @Override
                public Optional<CompletableFuture<?>> getVariable(String name) {
                    if (variables.containsKey(name)) {
                        return Optional.ofNullable(variables.get(name));
                    }
                    return parent.getVariable(name);
                }
            };
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
}
