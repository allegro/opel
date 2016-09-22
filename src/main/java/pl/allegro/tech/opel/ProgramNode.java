package pl.allegro.tech.opel;


import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProgramNode implements OpelNode {
    private DeclarationsListStatementNode declarations;
    private OpelNode expression;

    public ProgramNode(DeclarationsListStatementNode declarationsList, OpelNode expression) {
        this.declarations = declarationsList;
        this.expression = expression;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        return expression.getValue(updatedContext(declarations.getDeclarations(), context));
    }

    private EvalContext updatedContext(List<DeclarationStatementNode> declarations, EvalContext parentContext) {
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
            if (variables.containsKey(d.getIdentifier().getIdentifier())) {
                throw new OpelException("Illegal override of variable " + d.getIdentifier().getIdentifier());
            }
            variables.put(d.getIdentifier().getIdentifier(), value);
        });

        return updatedContext;
    }
}
