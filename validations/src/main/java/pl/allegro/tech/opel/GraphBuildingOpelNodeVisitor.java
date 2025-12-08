package pl.allegro.tech.opel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class GraphBuildingOpelNodeVisitor implements OpelNodeVisitor {

    public Map<OpelNode, TreeNode> nodes = new HashMap<>();
    Map<String, OpelNode> identifiers = new HashMap<>();

    public GraphBuildingOpelNodeVisitor(OpelEngine opelEngine) {
        opelEngine.embeddedEvalContext.getKeys().forEach(key -> {
            ExternalIdentifierExpressionNode identifierNode = new ExternalIdentifierExpressionNode(key);
            identifiers.put(key, identifierNode);
            nodes.put(identifierNode, new TreeNode(
                    identifierNode,
                    List.of()
            ));
        });
    }

    @Override
    public void visit(AnonymousFunctionExpressionNode node) {
        //opelNodes.put(node.)
    }

    @Override
    public void visit(ArgsGroupNode node) {
        node.argsGroup.forEach(it -> it.accept(this));
        TreeNode treeNode = new TreeNode(
                node,
                node.argsGroup.stream().map(arg -> nodes.get(arg)).toList()
        );
        nodes.put(node, treeNode);
    }

    @Override
    public void visit(ArgumentsListExpressionNode node) {
        node.args.forEach(it -> it.accept(this));
        TreeNode treeNode = new TreeNode(
                node,
                node.args.stream().map(arg -> nodes.get(arg)).toList()
        );
        nodes.put(node, treeNode);
    }

    @Override
    public void visit(BinaryOperationExpressionNode node) {

    }

    @Override
    public void visit(CompareOperatorExpressionNode node) {

    }

    @Override
    public void visit(DeclarationsListStatementNode node) {
        if (node.declarations.isEmpty()) {
            return;
        }
        node.declarations.forEach(declaration -> declaration.accept(this));
        /*TreeNode treeNode = new TreeNode(
                node,
                node.declarations.stream().map(declaration -> nodes.get(declaration)).toList()
        );
        nodes.put(node, treeNode);*/
    }

    @Override
    public void visit(DeclarationStatementNode node) {
        node.expression.accept(this);
        TreeNode identifierNode = new TreeNode(
                node.identifier,
                List.of(nodes.get(node.expression))
        );
        identifiers.put(node.identifier.getIdentifier(), node);
        nodes.put(node.identifier, identifierNode);
        TreeNode treeNode = new TreeNode(
                node,
                List.of(identifierNode)
        );
        nodes.put(node, treeNode);
    }

    @Override
    public void visit(DivideOperatorExpressionNode node) {

    }

    @Override
    public void visit(EqualOperatorExpressionNode node) {

    }

    @Override
    public void visit(FieldAccessExpressionNode node) {
        if (!(node.subject instanceof IdentifierExpressionNode)) {
            node.subject.accept(this);
        }
        TreeNode treeNode = new TreeNode(
                node,
                List.of(
                        nodes.get(node.subject)
         //               nodes.get(node.fieldName)
                )
        );
        nodes.put(node, treeNode);
    }

    @Override
    public void visit(FunctionCallExpressionNode node) {
        node.arguments.ifPresent(arguments -> arguments.accept(this));
        TreeNode treeNode = new TreeNode(
                node,
                node.arguments.map(arguments -> List.of(nodes.get(arguments))).orElse(List.of())
        );
        nodes.put(node, treeNode);
    }

    @Override
    public void visit(FunctionChainExpressionNode node) {
        node.argsGroups.accept(this);
        node.expression.accept(this);
        TreeNode treeNode = new TreeNode(
                node,
                List.of(
                        nodes.get(node.argsGroups),
                        nodes.get(node.expression)
                )
        );
        nodes.put(node, treeNode);
    }

    @Override
    public void visit(FunctionInstantiationExpressionNode node) {

    }

    @Override
    public void visit(IdentifierExpressionNode node) {
        throw new IllegalStateException("Identifier nodes should be always handles by node referring to them!");
    }

    @Override
    public void visit(IdentifiersListNode node) {

    }

    @Override
    public void visit(IfExpressionNode node) {
        node.condition.accept(this);
        node.left.accept(this);
        node.right.accept(this);
        TreeNode treeNode = new TreeNode(
                node,
                List.of(
                        nodes.get(node.condition),
                        nodes.get(node.left),
                        nodes.get(node.right)
                )
        );
        nodes.put(node, treeNode);
    }

    @Override
    public void visit(ImplicitConversion node) {

    }

    @Override
    public void visit(ListInstantiationExpressionNode node) {
        node.listElements.accept(this);
        TreeNode treeNode = new TreeNode(
                node,
                List.of(nodes.get(node.listElements))
        );
        nodes.put(node, treeNode);
    }

    @Override
    public void visit(LiteralExpressionNode node) {
        nodes.put(node, new TreeNode(
                node,
                List.of()
        ));
    }

    @Override
    public void visit(LogicalNegationOperatorExpressionNode node) {

    }

    @Override
    public void visit(LogicalOperatorExpressionNode node) {

    }

    @Override
    public void visit(MapAccessExpressionNode node) {

    }

    @Override
    public void visit(MapInstantiationExpressionNode node) {

    }

    @Override
    public void visit(MethodCallExpressionNode node) {
        node.subject.accept(this);
        node.arguments.ifPresent(arguments -> arguments.accept(this));
        TreeNode treeNode = new TreeNode(
                node,
                Stream.concat(
                        Stream.of(nodes.get(node.subject)),
                        node.arguments.stream().map(arguments -> nodes.get(arguments))
                ).toList()
        );
        nodes.put(node, treeNode);
    }

    @Override
    public void visit(MinusOperatorExpressionNode node) {

    }

    @Override
    public void visit(MultiplyOperatorExpressionNode node) {

    }

    @Override
    public void visit(NegationOperatorExpressionNode node) {

    }

    @Override
    public void visit(PairNode node) {

    }

    @Override
    public void visit(PairsListNode node) {

    }

    @Override
    public void visit(ProgramNode node) {
        node.declarations.accept(this);
        node.expression.accept(this);
        TreeNode treeNode = new TreeNode(
                node,
                List.of(nodes.get(node.expression))
        );
        nodes.put(node, treeNode);
    }

    @Override
    public void visit(StatementNode node) {

    }

    @Override
    public void visit(SumOperatorExpressionNode node) {

    }

    @Override
    public void visit(ValueExpressionNode node) {
        OpelNode identifier = identifiers.get(node.node.getIdentifier());

        TreeNode treeNode = new TreeNode(
                node,
                List.of(nodes.get(identifier))
        );
        nodes.put(node, treeNode);
    }

    static class ExternalIdentifierExpressionNode extends IdentifierExpressionNode {
        public ExternalIdentifierExpressionNode(String identifier) {
            super(identifier);
        }
    }
}
