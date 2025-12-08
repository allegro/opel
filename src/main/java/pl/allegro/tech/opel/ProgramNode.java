package pl.allegro.tech.opel;


import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProgramNode implements OpelNode {
    DeclarationsListStatementNode declarations;
    OpelNode expression;

    public ProgramNode(DeclarationsListStatementNode declarationsList, OpelNode expression) {
        this.declarations = declarationsList;
        this.expression = expression;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        return expression.getValue(updatedContext(declarations.getDeclarations(), context));
    }

    List<DeclarationStatementNode> getDeclarations() {
        return declarations.getDeclarations();
    }

    private EvalContext updatedContext(List<DeclarationStatementNode> declarations, EvalContext externalContext) {
        EvalContextBuilder contextBuilder = EvalContextBuilder.create();
        for (DeclarationStatementNode declaration : declarations) {
            String name = declaration.getIdentifier().getIdentifier();
            if (contextBuilder.hasValue(name)) {
                throw new OpelException("Illegal override of value " + declaration.getIdentifier().getIdentifier());
            }
            EvalContext valExpressionContext = EvalContextBuilder.mergeContexts(contextBuilder.build(), externalContext);
            CompletableFuture<Object> value = declaration.getExpression().getValue(valExpressionContext)
                    .thenApply(Function.identity());
            contextBuilder.withValue(name, value);
        }
        return contextBuilder.withExternalEvalContext(externalContext).build();
    }

    @Override
    public List<IdentifierExpressionNode> getRequiredIdentifiers() {
        //what to do with identifiers used inside function body but declared in arguments list?
        var argumentListIdentifiers = declarations.getDeclaredIdentifiers().stream()
                .map (IdentifierExpressionNode::getIdentifier).collect(Collectors.toSet());
        return Stream.concat(declarations.getRequiredIdentifiers().stream(), expression.getRequiredIdentifiers().stream())
                .filter(it ->
                    !argumentListIdentifiers.contains(it.getIdentifier())
            ).toList();
    }

    @Override
    public List<IdentifierExpressionNode> getDeclaredIdentifiers() {
        return OpelNode.super.getDeclaredIdentifiers();
    }

    @Override
    public void accept(OpelNodeVisitor visitor) {
        visitor.visit(this);
    }
}
