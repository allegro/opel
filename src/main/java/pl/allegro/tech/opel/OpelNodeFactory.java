package pl.allegro.tech.opel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class OpelNodeFactory {
    private final ImplicitConversion implicitConversion;
    private final MethodExecutionFilter methodExecutionFilter;
    private final WeakCache<OpelNode> nodeCache = new WeakCache<>();

    public OpelNodeFactory(ImplicitConversion implicitConversion, MethodExecutionFilter methodExecutionFilter) {
        this.implicitConversion = implicitConversion;
        this.methodExecutionFilter = methodExecutionFilter;
    }

    public OpelNode binaryOperationNode(Operator operator, OpelNode left, OpelNode right) {
        return nodeCache.get(
            BinaryOperationExpressionNode.class,
            new Object[]{operator, left, right},
            () -> operator.createNode(left, right, implicitConversion)
        );
    }

    public OpelNode ifNode(OpelNode condition, OpelNode trueValue, OpelNode falseValue) {
        return nodeCache.get(
            IfExpressionNode.class,
            new Object[]{condition, trueValue, falseValue},
            () -> new IfExpressionNode(condition, trueValue, falseValue, implicitConversion)
        );
    }

    public OpelNode negationNode(OpelNode node) {
        return nodeCache.get(
            NegationOperatorExpressionNode.class,
            new Object[]{node},
            () -> new NegationOperatorExpressionNode(node, implicitConversion)
        );
    }

    public OpelNode literalNode(Object value) {
        return nodeCache.get(
            LiteralExpressionNode.class,
            new Object[]{value},
            () -> new LiteralExpressionNode(value)
        );
    }

    public OpelNode fieldAccess(OpelNode subject, OpelNode fieldName) {
        return nodeCache.get(
            FieldAccessExpressionNode.class,
            new Object[]{subject, fieldName},
            () -> new FieldAccessExpressionNode(subject, fieldName)
        );
    }

    public OpelNode mapAccess(OpelNode subject, OpelNode fieldName) {
        return nodeCache.get(
            MapAccessExpressionNode.class,
            new Object[]{subject, fieldName},
            () -> new MapAccessExpressionNode(subject, fieldName)
        );
    }

    public OpelNode functionCallNode(OpelNode identifier, OpelNode functionArguments) {
        return nodeCache.get(
            FunctionCallExpressionNode.class,
            new Object[]{identifier, functionArguments},
            () -> FunctionCallExpressionNode.create(identifier, functionArguments)
        );
    }

    public OpelNode anonymousFunctionCallNode(OpelNode expression, OpelNode functionArguments) {
        return nodeCache.get(
            AnonymousFunctionExpressionNode.class,
            new Object[]{expression, functionArguments},
            () -> new AnonymousFunctionExpressionNode(expression, (ArgumentsListExpressionNode) functionArguments)
        );
    }

    public OpelNode methodCall(OpelNode subject, OpelNode methodName, OpelNode functionArguments) {
        return nodeCache.get(
            MethodCallExpressionNode.class,
            new Object[]{subject, methodName, functionArguments},
            () -> MethodCallExpressionNode.create(subject, methodName, functionArguments, implicitConversion, methodExecutionFilter)
        );
    }

    public ArgumentsListExpressionNode emptyArgumentsList() {
        return (ArgumentsListExpressionNode) nodeCache.get(
            ArgumentsListExpressionNode.class,
            new Object[]{},
            ArgumentsListExpressionNode::empty
        );
    }

    public ArgumentsListExpressionNode argumentsList(OpelNode args, OpelNode arg) {
        return (ArgumentsListExpressionNode) nodeCache.get(
            ArgumentsListExpressionNode.class,
            new Object[]{args, arg},
            () -> {
                ArrayList<OpelNode> allArgs = new ArrayList<>(((ArgumentsListExpressionNode) args).getArgs());
                allArgs.add(arg);
                return new ArgumentsListExpressionNode(allArgs);
            }
        );
    }

    public IdentifierExpressionNode identifierNode(String identifier) {
        return (IdentifierExpressionNode) nodeCache.get(
            IdentifierExpressionNode.class,
            new Object[]{identifier},
            () -> new IdentifierExpressionNode(identifier)
        );
    }

    public OpelNode namedValueNode(OpelNode valueIdentifierNode) {
        return nodeCache.get(
            ValueExpressionNode.class,
            new Object[]{valueIdentifierNode},
            () -> ValueExpressionNode.create(valueIdentifierNode)
        );
    }

    public DeclarationsListStatementNode emptyDeclarationsList() {
        return (DeclarationsListStatementNode) nodeCache.get(
            DeclarationsListStatementNode.class,
            new Object[]{},
            () -> new DeclarationsListStatementNode(Collections.emptyList())
        );
    }

    public DeclarationsListStatementNode declarationsList(OpelNode declarationsListExpressionNode, OpelNode identifier, OpelNode expression) {
        return (DeclarationsListStatementNode) nodeCache.get(
            DeclarationsListStatementNode.class,
            new Object[]{declarationsListExpressionNode, identifier, expression},
            () -> {
                List<DeclarationStatementNode> declarations = ((DeclarationsListStatementNode) declarationsListExpressionNode).getDeclarations();
                return new DeclarationsListStatementNode(declarations, new DeclarationStatementNode(identifier, expression));
            }
        );
    }

    public ProgramNode program(OpelNode declarationsList, OpelNode expression) {
        return (ProgramNode) nodeCache.get(
            ProgramNode.class,
            new Object[]{declarationsList, expression},
            () -> new ProgramNode((DeclarationsListStatementNode)declarationsList, expression)
        );
    }

    public ListInstantiationExpressionNode listInstantiation(OpelNode listElements) {
        return (ListInstantiationExpressionNode) nodeCache.get(
            ListInstantiationExpressionNode.class,
            new Object[]{listElements},
            () -> new ListInstantiationExpressionNode((ArgumentsListExpressionNode) listElements)
        );
    }

    public OpelNode functionInstantiation(OpelNode arguments, OpelNode body) {
        return nodeCache.get(
            FunctionInstantiationExpressionNode.class,
            new Object[]{arguments, body},
            () -> new FunctionInstantiationExpressionNode((IdentifiersListNode) arguments, body)
        );
    }

    public OpelNode emptyIdentifiersList() {
        return nodeCache.get(
            IdentifiersListNode.class,
            new Object[]{},
            IdentifiersListNode::empty
        );
    }

    public OpelNode identifiersList(OpelNode identifiers, OpelNode identifier) {
        return nodeCache.get(
            IdentifiersListNode.class,
            new Object[]{identifiers, identifier},
            () -> {
                ArrayList<OpelNode> allArgs = new ArrayList<>(((IdentifiersListNode) identifiers).getIdentifiers());
                allArgs.add(identifier);
                return new IdentifiersListNode(allArgs);
            }
        );
    }

    public OpelNode emptyArgsGroup() {
        return nodeCache.get(
            ArgsGroupNode.class,
            new Object[]{},
            ArgsGroupNode::empty
        );
    }

    public OpelNode argsGroup(OpelNode argsGroups, OpelNode argsGroup) {
        return nodeCache.get(
            ArgsGroupNode.class,
            new Object[]{argsGroups, argsGroup},
            () -> {
                List<ArgumentsListExpressionNode> allGroups = new ArrayList<>(((ArgsGroupNode) argsGroups).getGroups());
                allGroups.add((ArgumentsListExpressionNode) argsGroup);
                return new ArgsGroupNode(allGroups);
            }
        );
    }

    public OpelNode argsGroup(OpelNode argsGroup) {
        return nodeCache.get(
            ArgsGroupNode.class,
            new Object[]{argsGroup},
            () -> new ArgsGroupNode(Collections.singletonList((ArgumentsListExpressionNode) argsGroup))
        );
    }

    public OpelNode functionChain(OpelNode expression, OpelNode argsGroups) {
        return nodeCache.get(
            FunctionChainExpressionNode.class,
            new Object[]{expression, argsGroups},
            () -> new FunctionChainExpressionNode(expression, (ArgsGroupNode) argsGroups)
        );
    }

    public PairsListNode pairs(OpelNode pairs, OpelNode key, OpelNode value) {
        return (PairsListNode) nodeCache.get(
            PairsListNode.class,
            new Object[]{pairs, key, value},
            () -> {
                ArrayList<PairNode> allPairs = new ArrayList<>(((PairsListNode) pairs).getPairs());
                allPairs.add(new PairNode(key, value));
                return new PairsListNode(allPairs);
            }
        );
    }

    public PairsListNode emptyPairsListNode() {
        return (PairsListNode) nodeCache.get(
            PairsListNode.class,
            new Object[]{},
            () -> new PairsListNode(Collections.emptyList())
        );
    }

    public OpelNode mapInstantiationExpressionNode(OpelNode pairs) {
        return nodeCache.get(
            MapInstantiationExpressionNode.class,
            new Object[]{pairs},
            () -> new MapInstantiationExpressionNode((PairsListNode)pairs)
        );
    }

    public OpelNode logicalNegationOperatorExpressionNode(OpelNode value) {
        return nodeCache.get(
            LogicalNegationOperatorExpressionNode.class,
            new Object[]{value},
            () -> new LogicalNegationOperatorExpressionNode(value, implicitConversion)
        );
    }
}
