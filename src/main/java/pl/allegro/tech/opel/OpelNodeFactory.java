package pl.allegro.tech.opel;

import java.util.Collections;
import java.util.List;

class OpelNodeFactory {
    private final ImplicitConversion implicitConversion;
    private final MethodExecutionFilter methodExecutionFilter;

    public OpelNodeFactory(ImplicitConversion implicitConversion, MethodExecutionFilter methodExecutionFilter) {
        this.implicitConversion = implicitConversion;
        this.methodExecutionFilter = methodExecutionFilter;
    }

    public OpelNode binaryOperationNode(Operator operator, OpelNode left, OpelNode right) {
        return operator.createNode(left, right, implicitConversion);
    }

    public OpelNode ifNode(OpelNode condition, OpelNode trueValue, OpelNode falseValue) {
        return new IfExpressionNode(condition, trueValue, falseValue, implicitConversion);
    }

    public OpelNode negationNode(OpelNode node) {
        return new NegationOperatorExpressionNode(node, implicitConversion);
    }

    public OpelNode literalNode(Object value) {
        return new LiteralExpressionNode(value);
    }

    public OpelNode fieldAccess(OpelNode subject, OpelNode fieldName) {
        return new FieldAccessExpressionNode(subject, fieldName);
    }

    public OpelNode mapAccess(OpelNode subject, OpelNode fieldName) {
        return new MapAccessExpressionNode(subject, fieldName);
    }

    public OpelNode functionCallNode(OpelNode pop, ArgumentsListExpressionNode functionArguments) {
        return FunctionCallExpressionNode.create(pop, functionArguments);
    }

    public OpelNode functionCallNode(OpelNode pop) {
        return FunctionCallExpressionNode.create(pop);
    }

    public OpelNode methodCall(OpelNode subject, OpelNode methodName, ArgumentsListExpressionNode functionArguments) {
        return MethodCallExpressionNode.create(subject, methodName, functionArguments, implicitConversion, methodExecutionFilter);
    }

    public OpelNode methodCall(OpelNode subject, OpelNode methodName) {
        return MethodCallExpressionNode.create(subject, methodName, implicitConversion, methodExecutionFilter);
    }

    public ArgumentsListExpressionNode argumentsList(OpelNode head, ArgumentsListExpressionNode tail) {
        return new ArgumentsListExpressionNode(head, tail);
    }

    public ArgumentsListExpressionNode argumentsList(OpelNode head) {
        return new ArgumentsListExpressionNode(head);
    }

    public IdentifierExpressionNode identifierNode(String identifier) {
        return new IdentifierExpressionNode(identifier);
    }

    public OpelNode namedValueNode(OpelNode valueIdentifierNode) {
        return ValueExpressionNode.create(valueIdentifierNode);
    }

    public DeclarationStatementNode declaration(OpelNode identifier, OpelNode expression) {
        return new DeclarationStatementNode(identifier, expression);
    }

    public DeclarationsListStatementNode emptyDeclarationsList() {
        return new DeclarationsListStatementNode(Collections.emptyList());
    }

    public DeclarationsListStatementNode declarationsList(OpelNode declarationsListExpressionNode, OpelNode identifier, OpelNode expression) {
        List<DeclarationStatementNode> declarations = ((DeclarationsListStatementNode) declarationsListExpressionNode).getDeclarations();
        return new DeclarationsListStatementNode(declarations, new DeclarationStatementNode(identifier, expression));
    }

    public OpelNode program(OpelNode declarationsList, OpelNode expression) {
        return new ProgramNode((DeclarationsListStatementNode)declarationsList, expression);
    }
}
