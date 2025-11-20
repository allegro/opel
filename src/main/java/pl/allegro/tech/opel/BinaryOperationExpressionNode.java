package pl.allegro.tech.opel;

import java.util.List;

abstract class BinaryOperationExpressionNode implements OpelNode {
    private final OpelNode left;
    private final OpelNode right;

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
}
