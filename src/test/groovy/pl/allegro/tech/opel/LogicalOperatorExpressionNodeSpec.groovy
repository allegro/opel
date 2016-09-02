package pl.allegro.tech.opel

import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.function.Supplier

class LogicalOperatorExpressionNodeSpec extends Specification {

    @Unroll
    def 'should return #result when evaluating: #left || #right'() {
        given:
        def conversion = new ImplicitConversion()
        def expressionNode = LogicalOperatorExpressionNode.orOperator(valueNode(left), valueNode(right), conversion)

        expect:
        expressionNode.getValue().get() == result

        where:
        left  | right || result
        false | true  || true
        true  | false || true
        true  | true  || true
        false | false || false
    }

    @Unroll
    def 'should return #result when evaluating: #left && #right'() {
        given:
        def conversion = new ImplicitConversion()
        def expressionNode = LogicalOperatorExpressionNode.andOperator(valueNode(left), valueNode(right), conversion)

        expect:
        expressionNode.getValue().get() == result

        where:
        left  | right || result
        false | true  || false
        true  | false || false
        true  | true  || true
        false | false || false
    }

    def 'should convert operator argument to boolean if implicit conversion exists'() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(Foo, Boolean, { true }))
        def expressionNode = LogicalOperatorExpressionNode.orOperator(valueNode(new Foo()), valueNode(false), conversion)

        expect:
        expressionNode.getValue().get() == true
    }

    def 'should throw an exception if implicit conversion for condition argument not exist'() {
        given:
        def conversion = new ImplicitConversion()
        def expressionNode = LogicalOperatorExpressionNode.orOperator(valueNode(new Foo()), valueNode(false), conversion)

        when:
        expressionNode.getValue().get()

        then:
        thrown ExecutionException
    }

    def 'should treat nulls as false'() {
        given:
        def conversion = new ImplicitConversion()
        def expressionNode = LogicalOperatorExpressionNode.orOperator(valueNode(left), valueNode(right), conversion)

        expect:
        expressionNode.getValue().get() == result

        where:
        left | right || result
        null | true  || true
        true | null  || true
        null | null  || false
    }

    def 'logical operators should be lazy'() {
        given:
        def conversion = new ImplicitConversion()
        def numberOfConditionsChecked = 0;
        def trueValue = valueExpression({ numberOfConditionsChecked++; return true; })
        def falseValue = valueExpression({ numberOfConditionsChecked++; return false; })

        expect:
        LogicalOperatorExpressionNode.orOperator(trueValue, falseValue, conversion).getValue().get() == true
        LogicalOperatorExpressionNode.andOperator(falseValue, trueValue, conversion).getValue().get() == false

        and:
        numberOfConditionsChecked == 2
    }

    private static valueExpression(Supplier<Object> valueSupplier) {
        return {
            return CompletableFuture.completedFuture(valueSupplier.get());
        }
    }

    static class Foo {

    }

    private static ValueExpressionNode valueNode(Object o) {
        new ValueExpressionNode(o);
    }
}
