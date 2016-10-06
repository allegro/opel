package pl.allegro.tech.opel;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.SuppressSubnodes;

import java.math.BigDecimal;

@BuildParseTree
class OpelParser extends BaseParser<OpelNode> {

    final ImplicitConversion implicitConversion;
    final OpelNodeFactory nodeFactory;

    OpelParser(MethodExecutionFilter methodExecutionFilter, ImplicitConversion implicitConversion) {
        this.nodeFactory = new OpelNodeFactory(implicitConversion, methodExecutionFilter);
        this.implicitConversion = implicitConversion;
    }

    Rule ParsingUnit() {
        return Sequence(WhiteSpace(), Program(), EOI);
    }

    Rule Program() {
        return Sequence(Declarations(), Expression(), Optional("; "), push(nodeFactory.program(pop(1), pop())));
    }

    Rule Factor() {
        return FirstOf(
                ifExpression(),
                Sequence(Object(), Train()),
                Number(),
                NegativeNumber()
        );
    }

    Rule Object() {
        return FirstOf(FunctionCall(),StringLiteral(), NamedValue(), ListInstantiation(), Sequence("( ", Expression(), ") "));
    }

    Rule Train() {
        return ZeroOrMore(FirstOf(MethodCall(), FieldAccess()));
    }

    Rule ifExpression() {
        return Sequence("if ", "( ", Expression(), ") ", Expression(), "else ", Expression(),
                push(nodeFactory.ifNode(pop(2), pop(1), pop())));
    }

    Rule NamedValue() {
        return Sequence(Identifier(), push(namedValueNode(pop())));
    }

    Rule MethodCall() {
        return Sequence(". ", Identifier(), "( ", Args(), ") ",
                push(nodeFactory.methodCall(pop(2), pop(1), pop())));
    }

    Rule FieldAccess() {
        return FirstOf(
                Sequence("[ ", Expression(), "] ", push(nodeFactory.mapAccess(pop(1), pop()))),
                Sequence(". ", Identifier(), push(nodeFactory.fieldAccess(pop(1), pop())))
        );
    }

    Rule StringLiteral() {
        return Sequence("'", StringContent(), "'", push(pop()), WhiteSpace());
    }

    Rule StringContent() {
        return Sequence(ZeroOrMore(Sequence(TestNot(AnyOf("\r\n'")), ZeroOrMore(escapedChar()), ANY)), push(nodeFactory.literalNode(escapeString(matchOrDefault("")))));
    }

    Rule escapedChar() {
        return Sequence("\\", ANY);
    }

    Rule FunctionCall() {
        return Sequence(Identifier(), "( ", Args(), ") ",
                push(nodeFactory.functionCallNode(pop(1), pop())));
    }

    Rule Args() {
        return Sequence(
                push(nodeFactory.emptyArgumentsList()),
                FirstOf(Sequence(Arg(), ZeroOrMore(", ", Arg())), EMPTY));
    }

    Rule Arg() {
        return Sequence(Expression(), push(nodeFactory.argumentsList(pop(1), pop())));
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
                Factor(),
                ZeroOrMore(
                        FirstOf(
                                Sequence("* ", Factor(), push(binaryOperation(Operator.MULTIPLY))),
                                Sequence("/ ", Factor(), push(binaryOperation(Operator.DIV)))
                        )
                )
        );
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
                Factor(),
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
                push(nodeFactory.literalNode(new BigDecimal(match()))),
                WhiteSpace()
        );
    }

    Rule IntNumber() {
        return Sequence(
                Digits(),
                push(nodeFactory.literalNode(BigDecimal.valueOf(Integer.parseInt(matchOrDefault("0"))))),
                WhiteSpace()
        );
    }

    Rule ListInstantiation() {
        return Sequence(
                "[ ",
                Args(),
                "] ",
                push(nodeFactory.listInstantiation(pop())));

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

    protected OpelNode binaryOperation(Operator operator) {
        return nodeFactory.binaryOperationNode(operator, pop(1), pop());
    }

    protected OpelNode namedValueNode(OpelNode valueIdentifierNode) {
        if (valueIdentifierNode instanceof IdentifierExpressionNode) {
            String identifier = ((IdentifierExpressionNode) valueIdentifierNode).getIdentifier();
            switch (identifier) {
                case "true":
                    return nodeFactory.literalNode(true);
                case "false":
                    return nodeFactory.literalNode(false);
                case "null":
                    return nodeFactory.literalNode(null);
            }
        }
        return nodeFactory.namedValueNode(valueIdentifierNode);
    }
}
