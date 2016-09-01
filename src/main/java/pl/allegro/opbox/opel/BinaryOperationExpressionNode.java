package pl.allegro.opbox.opel;

abstract class BinaryOperationExpressionNode implements ExpressionNode {
    private final ExpressionNode left;
    private final ExpressionNode right;

    BinaryOperationExpressionNode(ExpressionNode left, ExpressionNode right) {
        this.left = left;
        this.right = right;
    }

    ExpressionNode left() {
        return left;
    }

    ExpressionNode right() {
        return right;
    }
}
