package pl.allegro.tech.opel

import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.CompletionException

class LogicalNegationOperatorExpressionNodeSpec extends Specification {

    @Unroll
    def 'should return negation of boolean'() {
        given:
        def conversion = new ImplicitConversion()
        def node = new LogicalNegationOperatorExpressionNode(new LiteralExpressionNode(booleanValue), conversion)

        expect:
        node.getValue(EvalContext.empty()).get() == !booleanValue

        where:
        booleanValue << [true, false]
    }

    @Unroll
    def 'should return negation of converted value'() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(convertion)
        def node = new LogicalNegationOperatorExpressionNode(new LiteralExpressionNode(new Foo()), conversion)

        expect:
        node.getValue(EvalContext.empty()).get() == expectedValue

        where:
        convertion                                            || expectedValue
        new ImplicitConversionUnit<>(Foo, Boolean, { true })  || false
        new ImplicitConversionUnit<>(Foo, Boolean, { false }) || true
    }

    def 'should thrown exception on negation of nulls'() {
        given:
        def conversion = new ImplicitConversion()
        def node = new LogicalNegationOperatorExpressionNode(new LiteralExpressionNode(null), conversion)

        when:
        node.getValue(EvalContext.empty()).join()

        then:
        def ex = thrown CompletionException
        ex.cause.class == OpelException
    }

    def 'should throw exception on negation of not boolean type'() {
        given:
        def conversion = new ImplicitConversion()
        def node = new LogicalNegationOperatorExpressionNode(new LiteralExpressionNode("some string"), conversion)

        when:
        node.getValue(EvalContext.empty()).join()

        then:
        def ex = thrown CompletionException
        ex.cause.class == OpelException
    }

    static class Foo {

    }
}
