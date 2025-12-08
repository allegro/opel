package pl.allegro.tech.opel;

import java.util.List;

abstract class BinaryOperationExpressionNode implements OpelNode {
    final OpelNode left;
    final OpelNode right;

    BinaryOperationExpressionNode(OpelNode left, OpelNode right) {
        this.left = left;
        this.right = right;
    }

    OpelNode left() {
        return left;
    }

    OpelNode right() {
        return right;
    }

    @Override
    public List<IdentifierExpressionNode> getRequiredIdentifiers() {
        return CollectionUtil.getIdentifiers(List.of(left, right));
    }

    @Override
    public void accept(OpelNodeVisitor visitor) {
        visitor.visit(this);
    }
}
