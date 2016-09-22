package pl.allegro.tech.opel;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.SuppressSubnodes;

import java.math.BigDecimal;

@BuildParseTree
class OpelParser extends BaseParser<OpelNode> {

    final ImplicitConversion implicitConversion;
    final ExpressionNodeFactory nodeFactory;

    OpelParser(MethodExecutionFilter methodExecutionFilter, ImplicitConversion implicitConversion) {
        nodeFactory = new ExpressionNodeFactory(implicitConversion, methodExecutionFilter);
        this.implicitConversion = implicitConversion;
    }

    Rule ParsingUnit() {
        return Sequence(WhiteSpace(), Program(), EOI);
    }

    Rule Value() {
        return FirstOf(
                ifExpression(),
                Sequence(
                        FunctionCall(),
                        ZeroOrMore(FirstOf(MethodCall(), ZeroArgumentMethodCall(), FieldAccess()))),
                Sequence(
                        StringLiteral(),
                        ZeroOrMore(FirstOf(MethodCall(), ZeroArgumentMethodCall()))),
                Sequence(
                        Variable(),
                        ZeroOrMore(FirstOf(MethodCall(), ZeroArgumentMethodCall(), FieldAccess()))),
                Number(),
                NegativeNumber(),
                Sequence("( ", Expression(), ") ")
        );
    }

    Rule ifExpression() {
        return Sequence("if ", "( ", Expression(), ") ", Expression(), "else ", Expression(),
                push(nodeFactory.ifNode(pop(2), pop(1), pop())));
    }

    Rule Variable() {
        return Sequence(Identifier(), push(variableNode(pop())));
    }

    Rule FunctionCall() {
        return FirstOf(ArgumentsFunctionCall(), ZeroArgumentFunctionCall());
    }

    Rule MethodCall() {
        return Sequence(". ", Identifier(), "( ", Args(), ") ",
                push(nodeFactory.methodCall(pop(2), pop(1), getFunctionArguments())));
    }

    Rule ZeroArgumentMethodCall() {
        return Sequence(". ", Identifier(), "( ", ") ",
                push(nodeFactory.methodCall(pop(1), pop())));
    }

    Rule FieldAccess() {
        return FirstOf(
                Sequence("[ ", AdditiveExpression(), "] ", push(nodeFactory.mapAccess(pop(1), pop()))),
                Sequence(". ", Identifier(), push(nodeFactory.fieldAccess(pop(1), pop())))
        );
    }

    Rule StringLiteral() {
        return Sequence("'", StringContent(), "'", push(pop()), WhiteSpace());
    }

    Rule StringContent() {
        return Sequence(ZeroOrMore(Sequence(TestNot(AnyOf("\r\n'")), ZeroOrMore(escapedChar()), ANY)), push(nodeFactory.valueNode(escapeString(matchOrDefault("")))));
    }

    Rule escapedChar() {
        return Sequence("\\", ANY);
    }

    Rule ZeroArgumentFunctionCall() {
        return Sequence(Identifier(), "( ", ") ",
                push(nodeFactory.functionCallNode(pop())));

    }

    Rule ArgumentsFunctionCall() {
        return Sequence(Identifier(), "( ", Args(), ") ",
                push(nodeFactory.functionCallNode(pop(1), getFunctionArguments())));
    }

    Rule Args() {
        return Sequence(Expression(),
                ZeroOrMore(", ", Expression(), push(nodeFactory.argumentsList(pop(), getFunctionArguments()))));
    }

    Rule Identifier() {
        return Sequence(
                Sequence(
                        OneOrMore(
                                Letter(),
                                ZeroOrMore(FirstOf(Letter(), Digit()))
                        ),
                        WhiteSpace()
                ),
                push(nodeFactory.identifierNode(match().trim())));
    }

    Rule AdditiveExpression() {
        return Sequence(
                MultiplyExpression(),
                ZeroOrMore(
                        FirstOf(
                                Sequence("+ ", MultiplyExpression(), push(binaryOperation(Operator.PLUS))),
                                Sequence("- ", MultiplyExpression(), push(binaryOperation(Operator.MINUS)))
                        )
                )
        );
    }

    Rule MultiplyExpression() {
        return Sequence(
                Value(),
                ZeroOrMore(
                        FirstOf(
                                Sequence("* ", Value(), push(binaryOperation(Operator.MULTIPLY))),
                                Sequence("/ ", Value(), push(binaryOperation(Operator.DIV)))
                        )
                )
        );
    }

    Rule Program() {
        return Sequence(Declarations(), Expression(), push(nodeFactory.program(pop(1), pop())));
    }

    Rule Declarations() {
        return Sequence(push(nodeFactory.emptyDeclarationsList()), ZeroOrMore(Declaration()));
    }

    Rule Declaration() {
        return Sequence("val ", Identifier(), "= ", Expression(), "; ", push(nodeFactory.declarationsList(pop(2), pop(1), pop())));
    }

    Rule Expression() {
        return OrExpression();
    }

    Rule OrExpression() {
        return Sequence(
                AndExpression(),
                ZeroOrMore(
                        Sequence("|| ", AndExpression(), push(binaryOperation(Operator.OR)))
                )
        );
    }

    Rule AndExpression() {
        return Sequence(
                EqualityExpression(),
                ZeroOrMore(
                        Sequence("&& ", EqualityExpression(), push(binaryOperation(Operator.AND)))
                )
        );
    }

    Rule EqualityExpression() {
        return Sequence(
                RelationalExpression(),
                ZeroOrMore(
                        FirstOf(
                                Sequence("== ", RelationalExpression(), push(binaryOperation(Operator.EQUAL))),
                                Sequence("!= ", RelationalExpression(), push(binaryOperation(Operator.NOT_EQUAL)))
                        )
                )
        );
    }

    Rule RelationalExpression() {
        return Sequence(
                AdditiveExpression(),
                ZeroOrMore(
                        FirstOf(
                                Sequence("> ", AdditiveExpression(), push(binaryOperation(Operator.GT))),
                                Sequence(">= ", AdditiveExpression(), push(binaryOperation(Operator.GTE))),
                                Sequence("< ", AdditiveExpression(), push(binaryOperation(Operator.LT))),
                                Sequence("<= ", AdditiveExpression(), push(binaryOperation(Operator.LTE)))
                        )
                )
        );
    }

    Rule NegativeNumber() {
        return Sequence(
                AnyOf("-"),
                Value(),
                push(nodeFactory.negationNode(pop())),
                WhiteSpace()
        );
    }

    Rule Number() {
        return FirstOf(
                DecimalNumber(),
                IntNumber()
        );
    }

    Rule DecimalNumber() {
        return Sequence(
                Sequence(
                        Digits(),
                        ".",
                        Digits()
                ),
                push(nodeFactory.valueNode(new BigDecimal(match()))),
                WhiteSpace()
        );
    }

    Rule IntNumber() {
        return Sequence(
                Digits(),
                push(nodeFactory.valueNode(BigDecimal.valueOf(Integer.parseInt(matchOrDefault("0"))))),
                WhiteSpace()
        );
    }

    @SuppressSubnodes
    Rule Digits() {
        return OneOrMore(Digit());
    }

    Rule Digit() {
        return CharRange('0', '9');
    }

    Rule Letter() {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), '_', '$');
    }

    Rule WhiteSpace() {
        return ZeroOrMore(AnyOf(" \t\n"));
    }

    // Handy method for handling whitespaces, see: https://github.com/sirthias/parboiled/wiki/Handling-Whitespace
    @Override
    protected Rule fromStringLiteral(String string) {
        return string.endsWith(" ") ?
                Sequence(String(string.substring(0, string.length() - 1)), WhiteSpace()) :
                String(string);
    }

    protected String escapeString(String string) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == '\\') {
                i++;
            }
            result.append(string.charAt(i));
        }
        return result.toString();
    }

    protected ArgumentsListExpressionNode getFunctionArguments() {

        OpelNode node = pop();
        if (node instanceof ArgumentsListExpressionNode) {
            return (ArgumentsListExpressionNode) node;
        }
        return nodeFactory.argumentsList(node);
    }

    protected OpelNode binaryOperation(Operator operator) {
        return nodeFactory.binaryOperationNode(operator, pop(1), pop());
    }

    protected OpelNode variableNode(OpelNode variableIdentifierNode) {
        if (variableIdentifierNode instanceof IdentifierExpressionNode) {
            String identifier = ((IdentifierExpressionNode) variableIdentifierNode).getIdentifier();
            switch (identifier) {
                case "true":
                    return nodeFactory.valueNode(true);
                case "false":
                    return nodeFactory.valueNode(false);
                case "null":
                    return nodeFactory.valueNode(null);
            }
        }
        return nodeFactory.variableNode(variableIdentifierNode);
    }
}
