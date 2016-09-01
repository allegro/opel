package pl.allegro.opbox.opel;

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

    public ExpressionNode createNode(ExpressionNode left, ExpressionNode right, ImplicitConversion implicitConversion) {
        switch (this) {
            case PLUS:
                return new SumOperatorExpressionNode(left, right, implicitConversion);
            case MINUS:
                return new MinusOperatorExpressionNode(left, right, implicitConversion);
            case MULTIPLY:
                return new MultiplyOperatorExpressionNode(left, right, implicitConversion);
            case DIV:
                return new DivideOperatorExpressionNode(left, right, implicitConversion);
            case GT:
                return CompareOperatorExpressionNode.greaterThen(left, right, implicitConversion);
            case GTE:
                return CompareOperatorExpressionNode.greaterOrEqual(left, right, implicitConversion);
            case LT:
                return CompareOperatorExpressionNode.lowerThen(left, right, implicitConversion);
            case LTE:
                return CompareOperatorExpressionNode.lowerOrEqual(left, right, implicitConversion);
            case EQUAL:
                return EqualOperatorExpressionNode.equalityOperator(left, right, implicitConversion);
            case NOT_EQUAL:
                return EqualOperatorExpressionNode.inequalityOperator(left, right, implicitConversion);
            case AND:
                return LogicalOperatorExpressionNode.andOperator(left, right, implicitConversion);
            case OR:
                return LogicalOperatorExpressionNode.orOperator(left, right, implicitConversion);
        }
        // Can only happen when not all operators are listed above
        throw new UnsupportedOperationException("Unsupported operator " + this);
    }
}
