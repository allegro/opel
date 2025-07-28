package pl.allegro.tech.opel;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.DontLabel;
import org.parboiled.annotations.SuppressSubnodes;

import java.math.BigDecimal;

@BuildParseTree
public class OpelParser extends BaseParser<OpelNode> {

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
        return Body();
    }

    Rule Body() {
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
        return FirstOf(
                FunctionCallChain(),
                StringLiteral(),
                FunctionInstantiation(),
                NamedValue(),
                ListInstantiation(),
                MapInstantiation(),
                Sequence("( ", Expression(), ") ")
        );
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
        return FirstOf(
                Sequence("'", StringContent("'"), "'", push(pop()), WhiteSpace()),
                Sequence("\"", StringContent("\""), "\"", push(pop()), WhiteSpace())
        );
    }

    Rule StringContent(String quote) {
        return Sequence(
                ZeroOrMore(Sequence(TestNot(AnyOf("\r\n%s".formatted(quote))), FirstOf(escapedChar(), ANY))),
                push(nodeFactory.literalNode(escapeString(matchOrDefault(""))))
        );
    }

    Rule escapedChar() {
        return Sequence("\\", ANY);
    }

    Rule FunctionCallChain() {
        return Sequence(FunctionCall(), ArgsGroups(), push(nodeFactory.functionChain(pop(1), pop())));
    }

    Rule ArgsGroups() {
        return Sequence(
                push(nodeFactory.emptyArgsGroup()),
                ZeroOrMore(ArgsGroup(), EMPTY)
        );
    }

    Rule ArgsGroup() {
        return Sequence("( ", Args(), ") ", push(nodeFactory.argsGroup(pop(1), pop())));
    }

    Rule FunctionCall() {
        return FirstOf(
                Sequence(Identifier(), "( ", Args(), ") ", push(nodeFactory.functionCallNode(pop(1), pop()))),
                Sequence(FunctionInstantiation(), "( ", Args(), ") ", push(nodeFactory.anonymousFunctionCallNode(pop(1), pop()))),
                Sequence("( ", Expression(), ") ", "( ", Args(), ") ", push(nodeFactory.functionChain(pop(1), nodeFactory.argsGroup(pop()))))
        );
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
                LogicalNegation(),
                ZeroOrMore(
                        FirstOf(
                                Sequence("* ", LogicalNegation(), push(binaryOperation(Operator.MULTIPLY))),
                                Sequence("/ ", LogicalNegation(), push(binaryOperation(Operator.DIV)))
                        )
                )
        );
    }

    Rule LogicalNegation() {
        return FirstOf(
                Sequence("! ", LogicalNegation(), push(nodeFactory.logicalNegationOperatorExpressionNode(pop()))),
                Factor()
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

    Rule FunctionInstantiation() {
        return Sequence(FunctionArgs(), "-> ", FunctionBody(), push(nodeFactory.functionInstantiation(pop(1), pop())));
    }

    Rule FunctionArgs() {
        return FirstOf(
                Sequence("( ", IdentifiersList(), ") "),
                Sequence(push(nodeFactory.emptyIdentifiersList()), IdentifiersListItem())
        );
    }

    Rule IdentifiersList() {
        return Sequence(
                push(nodeFactory.emptyIdentifiersList()),
                FirstOf(Sequence(IdentifiersListItem(), ZeroOrMore(", ", IdentifiersListItem())), EMPTY));
    }

    Rule IdentifiersListItem() {
        return Sequence(Identifier(), push(nodeFactory.identifiersList(pop(1), pop())));
    }

    Rule FunctionBody() {
        return FirstOf(
                Expression(),
                Sequence("{ ", Body(), "} "));
    }

    Rule ListInstantiation() {
        return Sequence(
                "[ ",
                Args(),
                "] ",
                push(nodeFactory.listInstantiation(pop())));
    }

    Rule MapInstantiation() {
        return Sequence(
                "{ ",
                Pairs(),
                "} ",
                push(nodeFactory.mapInstantiationExpressionNode(pop()))
        );
    }

    Rule Pairs() {
        return Sequence(
                push(nodeFactory.emptyPairsListNode()),
                FirstOf(Sequence(Pair(), ZeroOrMore(", ", Pair())), ": ")
        );
    }

    Rule Pair() {
        return Sequence(Factor(), ": ", Expression(), push(nodeFactory.pairs(pop(2), pop(1), pop())));
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
    @DontLabel
    protected Rule fromStringLiteral(String string) {
        if (string.endsWith(" ")) {
            var substring = string.substring(0, string.length() - 1);
            return Sequence(String(substring), WhiteSpace()).label("'%s'".formatted(substring));
        }
        return String(string).label("'%s'".formatted(string));
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
