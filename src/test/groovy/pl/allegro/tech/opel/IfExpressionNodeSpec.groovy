package pl.allegro.tech.opel

import spock.lang.Specification

import java.util.concurrent.ExecutionException

class IfExpressionNodeSpec extends Specification {

    def 'should return left value when condition result is positive'() {
        given:
        def conversion = new ImplicitConversion()
        def expressionNode = new IfExpressionNode(completedVal(true), completedVal('a'), completedVal('b'), conversion)

        expect:
        expressionNode.getValue().get() == 'a'
    }

    def 'should return right value when condition result is negative'() {
        given:
        def conversion = new ImplicitConversion()
        def expressionNode = new IfExpressionNode(completedVal(false), completedVal('a'), completedVal('b'), conversion)

        expect:
        expressionNode.getValue().get() == 'b'
    }

    def 'should convert condition argument to boolean if implicit conversion exist'() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(Foo, Boolean, { true }))
        def expressionNode = new IfExpressionNode(completedVal(new Foo()), completedVal('a'), completedVal('b'), conversion)

        expect:
        expressionNode.getValue().get() == 'a'
    }

    def 'should throw exception if implicit conversion for condition argument not exist'() {
        given:
        def conversion = new ImplicitConversion()
        def expressionNode = new IfExpressionNode(completedVal(new Foo()), completedVal('a'), completedVal('b'), conversion)

        when:
        expressionNode.getValue().get()

        then:
        thrown ExecutionException
    }

    def 'should treat nulls as false'() {
        given:
        def conversion = new ImplicitConversion()
        def expressionNode = new IfExpressionNode(completedVal(null), completedVal('a'), completedVal('b'), conversion)

        expect:
        expressionNode.getValue().get() == 'b'
    }

    static class Foo {

    }

    private static ValueExpressionNode completedVal(Object o) {
        new ValueExpressionNode(o);
    }
}
