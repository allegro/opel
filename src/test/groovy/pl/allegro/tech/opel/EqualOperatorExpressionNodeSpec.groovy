package pl.allegro.tech.opel

import spock.lang.Specification
import spock.lang.Unroll

import static EqualOperatorExpressionNode.equalityOperator
import static EqualOperatorExpressionNode.inequalityOperator

class EqualOperatorExpressionNodeSpec extends Specification {

    @Unroll
    def "should calculate equality for nulls"() {
        given:
        def equalExpressionNode = equalityOperator(valueNode(left), valueNode(right), new ImplicitConversion())
        def notEqualExpressionNode = inequalityOperator(valueNode(left), valueNode(right), new ImplicitConversion())

        expect:
        equalExpressionNode.getValue().join() == result
        notEqualExpressionNode.getValue().join() != result

        where:
        left         | right        || result
        null         | null         || true
        null         | new Object() || false
        new Object() | null         || false
    }

    @Unroll
    def "should calculate equality for the same types"() {
        given:
        def equalExpressionNode = equalityOperator(valueNode(left), valueNode(right), new ImplicitConversion())
        def notEqualExpressionNode = inequalityOperator(valueNode(left), valueNode(right), new ImplicitConversion())

        expect:
        equalExpressionNode.getValue().join() == result
        notEqualExpressionNode.getValue().join() != result

        where:
        left  | right || result
        ""    | ""    || true
        "abc" | "abc" || true
        "abc" | "cba" || false
        5     | 5     || true
        5     | 6     || false
    }

    def "should prefer conversion to left type when types are different"() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(Foo, String, { 'foo' }))
        conversion.register(new ImplicitConversionUnit<>(String, Foo, { new Foo() }))
        def equalExpressionNode = equalityOperator(valueNode(left), valueNode(right), conversion)
        def notEqualExpressionNode = inequalityOperator(valueNode(left), valueNode(right), conversion)

        expect:
        equalExpressionNode.getValue().join() == result
        notEqualExpressionNode.getValue().join() != result

        where:
        left      | right     || result
        'foo'     | new Foo() || true
        new Foo() | 'foo'     || false //foo has no defined equals method
    }

    def "should convert left argument to right argument type if there is no conversion from right arg type to left arg type"() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(Foo, String, { 'foo' }))
        def equalExpressionNode = equalityOperator(valueNode(left), valueNode(right), conversion)
        def notEqualExpressionNode = inequalityOperator(valueNode(left), valueNode(right), conversion)

        expect:
        equalExpressionNode.getValue().join() == result
        notEqualExpressionNode.getValue().join() != result

        where:
        left      | right || result
        new Foo() | 'foo' || true
    }

    def "should not be equal if types are different and there is no required conversion"() {
        given:
        def conversion = new ImplicitConversion()
        def equalExpressionNode = equalityOperator(valueNode(left), valueNode(right), conversion)
        def notEqualExpressionNode = inequalityOperator(valueNode(left), valueNode(right), conversion)

        expect:
        equalExpressionNode.getValue().join() == result
        notEqualExpressionNode.getValue().join() != result

        where:
        left      | right     || result
        new Foo() | 'foo'     || false
        'foo'     | new Foo() || false
    }

    private static class Foo {}

    private static ValueExpressionNode valueNode(Object o) {
        new ValueExpressionNode(o);
    }
}