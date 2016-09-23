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
        EvalContextBuilder contextBuilder = EvalContextBuilder.create();
        for (DeclarationStatementNode declaration : declarations) {
            String name = declaration.getIdentifier().getIdentifier();
            if (contextBuilder.hasVariable(name)) {
                throw new OpelException("Illegal override of variable " + declaration.getIdentifier().getIdentifier());
            }
            EvalContext valExpressionContext = EvalContextBuilder.mergeContexts(contextBuilder.build(), parentContext);
            CompletableFuture<Object> value = declaration.getExpression().getValue(valExpressionContext)
                    .thenApply(Function.identity());
            contextBuilder.withVariable(name, value);
        }
        return contextBuilder.withParentEvalContext(parentContext).build();
    }
}
