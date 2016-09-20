package pl.allegro.tech.opel;


import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ProgramExpressionNode implements ExpressionNode {
    private DeclarationsListExpressionNode declarations;
    private ExpressionNode expression;

    public ProgramExpressionNode(DeclarationsListExpressionNode declarationsList, ExpressionNode expression) {
        this.declarations = declarationsList;
        this.expression = expression;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        return expression.getValue(updatedContext(declarations.getDeclarations(), context));
    }

    private EvalContext updatedContext(List<DeclarationExpressionNode> declarations, EvalContext parentContext) {
        if (declarations.isEmpty()) {
            return parentContext;
        }

        final Map<String, CompletableFuture<?>> variables = new HashMap<>();

        final EvalContext updatedContext = new EvalContext() {
            @Override
            public Optional<OpelAsyncFunction<?>> getFunction(String name) {
                return parentContext.getFunction(name);
            }

            @Override
            public Optional<CompletableFuture<?>> getVariable(String name) {
                if (variables.containsKey(name)) {
                    return Optional.of(variables.get(name));
                }
                return parentContext.getVariable(name);
            }
        };

        declarations.stream().forEach( d -> {
            CompletableFuture<?> value = d.getExpression().getValue(updatedContext);
            variables.put(d.getIdentifier().getIdentifier(), value);
        });

        return updatedContext;
    }
}
