package pl.allegro.tech.opel;

enum Operator {
    PLUS,
    MINUS,
    MULTIPLY,
    DIV,
    GT,
    GTE,
    LT,
    LTE,
    EQUAL,
    NOT_EQUAL,
    AND,
    OR;

    public OpelNode createNode(OpelNode left, OpelNode right, ImplicitConversion implicitConversion) {
        return switch (this) {
            case PLUS -> new SumOperatorExpressionNode(left, right, implicitConversion);
            case MINUS -> new MinusOperatorExpressionNode(left, right, implicitConversion);
            case MULTIPLY -> new MultiplyOperatorExpressionNode(left, right, implicitConversion);
            case DIV -> new DivideOperatorExpressionNode(left, right, implicitConversion);
            case GT -> CompareOperatorExpressionNode.greaterThen(left, right, implicitConversion);
            case GTE -> CompareOperatorExpressionNode.greaterOrEqual(left, right, implicitConversion);
            case LT -> CompareOperatorExpressionNode.lowerThen(left, right, implicitConversion);
            case LTE -> CompareOperatorExpressionNode.lowerOrEqual(left, right, implicitConversion);
            case EQUAL -> EqualOperatorExpressionNode.equalityOperator(left, right, implicitConversion);
            case NOT_EQUAL -> EqualOperatorExpressionNode.inequalityOperator(left, right, implicitConversion);
            case AND -> LogicalOperatorExpressionNode.andOperator(left, right, implicitConversion);
            case OR -> LogicalOperatorExpressionNode.orOperator(left, right, implicitConversion);
        };
        // Can only happen when not all operators are listed above
    }
}
