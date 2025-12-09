package pl.allegro.tech.opel;

interface OpelNodeVisitor {
    void visit(AnonymousFunctionExpressionNode node);
    void visit(ArgsGroupNode node);
    void visit(ArgumentsListExpressionNode node);
    void visit(BinaryOperationExpressionNode node);
    void visit(CompareOperatorExpressionNode node);
    void visit(DeclarationsListStatementNode node);
    void visit(DeclarationStatementNode node);
    void visit(DivideOperatorExpressionNode node);
    void visit(EqualOperatorExpressionNode node);
    void visit(FieldAccessExpressionNode node);
    void visit(FunctionCallExpressionNode node);
    void visit(FunctionChainExpressionNode node);
    void visit(FunctionInstantiationExpressionNode node);
    void visit(IdentifierExpressionNode node);
    void visit(IdentifiersListNode node);
    void visit(IfExpressionNode node);
    void visit(ImplicitConversion node);
    void visit(ListInstantiationExpressionNode node);
    void visit(LiteralExpressionNode node);
    void visit(LogicalNegationOperatorExpressionNode node);
    void visit(LogicalOperatorExpressionNode node);
    void visit(MapAccessExpressionNode node);
    void visit(MapInstantiationExpressionNode node);
    void visit(MethodCallExpressionNode node);
    void visit(MinusOperatorExpressionNode node);
    void visit(MultiplyOperatorExpressionNode node);
    void visit(NegationOperatorExpressionNode node);
    void visit(PairNode node);
    void visit(PairsListNode node);
    void visit(ProgramNode node);
    void visit(StatementNode node);
    void visit(SumOperatorExpressionNode node);
    void visit(ValueExpressionNode node);
}
