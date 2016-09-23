package pl.allegro.tech.opel;

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
}
