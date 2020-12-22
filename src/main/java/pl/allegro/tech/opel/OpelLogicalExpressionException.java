package pl.allegro.tech.opel;

public class OpelLogicalExpressionException extends OpelException {

    public OpelLogicalExpressionException(Operator logicalOperator, Object left, Exception exception) {
        super(getMessage(logicalOperator, left), exception);
    }

    public OpelLogicalExpressionException(Operator logicalOperator, Object left, Object right, Exception exception) {
        super(getMessage(logicalOperator, left, right), exception);
    }

    private static String getMessage(Operator logicalOperator, Object left) {
        return String.format(
                "Error on evaluating left side of logical expression. " +
                "Operator: '%s', left: '%s', class: '%s'",
                logicalOperator, left, left.getClass());
    }

    private static String getMessage(Operator logicalOperator, Object left, Object right) {
        return String.format(
                "Error on evaluating logical expression. " +
                "Operator: '%s', left: '%s', class: '%s' right: '%s', class: '%s'",
                logicalOperator, left, left.getClass(), right, right.getClass());
    }
}
