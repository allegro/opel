package pl.allegro.tech.opel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

class LogicalNegationOperatorExpressionNode implements OpelNode {

    OpelNode opelNode;
    private final ImplicitConversion conversion;

    LogicalNegationOperatorExpressionNode(OpelNode opelNode, ImplicitConversion conversion) {
        this.opelNode = opelNode;
        this.conversion = conversion;
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        return opelNode.getValue(context)
                .thenApply(this::getValue);
    }

    private Boolean getValue(Object value) {
        if (value == null) {
            throw new OpelException("Can't negate null value");
        } if (value instanceof Boolean) {
            return !((boolean)value);
        } else if (conversion.hasConverter(value, Boolean.class)) {
            return !conversion.convert(value, Boolean.class);
        }
        throw new OpelException("Can't negate " + value.getClass().getSimpleName() + " type");
    }

    @Override
    public List<IdentifierExpressionNode> getRequiredIdentifiers() {
        return opelNode.getRequiredIdentifiers();
    }

    @Override
    public void accept(OpelNodeVisitor visitor) {
        visitor.visit(this);
    }
}
