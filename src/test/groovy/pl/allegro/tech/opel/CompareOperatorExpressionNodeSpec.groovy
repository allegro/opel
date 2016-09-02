package pl.allegro.tech.opel

import spock.lang.Specification
import spock.lang.Unroll

import java.time.Duration

class CompareOperatorExpressionNodeSpec extends Specification {

    @Unroll
    def "should compare nulls with lower weight than other objects"() {
        given:
        def greaterOrEqualExpressionNode = CompareOperatorExpressionNode.greaterOrEqual(completedVal(left), completedVal(right), new ImplicitConversion())
        def greaterThenExpressionNode = CompareOperatorExpressionNode.greaterThen(completedVal(left), completedVal(right), new ImplicitConversion())
        def lowerOrEqualExpressionNode = CompareOperatorExpressionNode.lowerOrEqual(completedVal(left), completedVal(right), new ImplicitConversion())
        def lowerThenExpressionNode = CompareOperatorExpressionNode.lowerThen(completedVal(left), completedVal(right), new ImplicitConversion())

        expect:
        greaterOrEqualExpressionNode.getValue().join() == (result >= 0)
        greaterThenExpressionNode.getValue().join() == (result > 0)
        lowerOrEqualExpressionNode.getValue().join() == (result <= 0)
        lowerThenExpressionNode.getValue().join() == (result < 0)

        where:
        left         | right        || result
        null         | null         || 0
        null         | new Object() || -1
        new Object() | null         || 1
    }

    @Unroll
    def "should compare objects of the same type implementing comparable"() {
        given:
        def greaterOrEqualExpressionNode = CompareOperatorExpressionNode.greaterOrEqual(completedVal(left), completedVal(right), new ImplicitConversion())
        def greaterThenExpressionNode = CompareOperatorExpressionNode.greaterThen(completedVal(left), completedVal(right), new ImplicitConversion())
        def lowerOrEqualExpressionNode = CompareOperatorExpressionNode.lowerOrEqual(completedVal(left), completedVal(right), new ImplicitConversion())
        def lowerThenExpressionNode = CompareOperatorExpressionNode.lowerThen(completedVal(left), completedVal(right), new ImplicitConversion())

        expect:
        greaterOrEqualExpressionNode.getValue().join() == (result >= 0)
        greaterThenExpressionNode.getValue().join() == (result > 0)
        lowerOrEqualExpressionNode.getValue().join() == (result <= 0)
        lowerThenExpressionNode.getValue().join() == (result < 0)

        where:
        left                | right                || result
        "a"                 | "a"                  || 0
        "a"                 | "b"                  || -1
        "b"                 | "a"                  || 1
        1                   | 1                    || 0
        1                   | 2                    || -1
        Duration.ofHours(1) | Duration.ofHours(10) || -1
    }

    @Unroll
    def "should prefer conversion to left type when types are different"() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(Integer, String, { Integer.toString(it) }))
        conversion.register(new ImplicitConversionUnit<>(String, Integer, { Integer.parseInt(it) }))
        def greaterOrEqualExpressionNode = CompareOperatorExpressionNode.greaterOrEqual(completedVal(left), completedVal(right), conversion)
        def greaterThenExpressionNode = CompareOperatorExpressionNode.greaterThen(completedVal(left), completedVal(right), conversion)
        def lowerOrEqualExpressionNode = CompareOperatorExpressionNode.lowerOrEqual(completedVal(left), completedVal(right), conversion)
        def lowerThenExpressionNode = CompareOperatorExpressionNode.lowerThen(completedVal(left), completedVal(right), conversion)

        expect:
        greaterOrEqualExpressionNode.getValue().join() == (result >= 0)
        greaterThenExpressionNode.getValue().join() == (result > 0)
        lowerOrEqualExpressionNode.getValue().join() == (result <= 0)
        lowerThenExpressionNode.getValue().join() == (result < 0)

        where:
        left | right || result
        11   | "11"  || 0
        11   | "5"   || 1
        "11" | 5     || -1
    }

    @Unroll
    def "should convert left argument to right type if no conversion for right to left"() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(Foo, String, { '11' }))
        def greaterOrEqualExpressionNode = CompareOperatorExpressionNode.greaterOrEqual(completedVal(left), completedVal(right), conversion)
        def greaterThenExpressionNode = CompareOperatorExpressionNode.greaterThen(completedVal(left), completedVal(right), conversion)
        def lowerOrEqualExpressionNode = CompareOperatorExpressionNode.lowerOrEqual(completedVal(left), completedVal(right), conversion)
        def lowerThenExpressionNode = CompareOperatorExpressionNode.lowerThen(completedVal(left), completedVal(right), conversion)

        expect:
        greaterOrEqualExpressionNode.getValue().join() == (result >= 0)
        greaterThenExpressionNode.getValue().join() == (result > 0)
        lowerOrEqualExpressionNode.getValue().join() == (result <= 0)
        lowerThenExpressionNode.getValue().join() == (result < 0)

        where:
        left      | right || result
        new Foo() | "11"  || 0
        new Foo() | "5"   || -1
    }

    @Unroll
    def "should throw exception when none of arguments implements Comparable (even if converter to Comparable exist)"() {
        when:
        expressionNode.getValue().join()

        then:
        thrown RuntimeException

        where:
        expressionNode << [
                CompareOperatorExpressionNode.greaterOrEqual(completedVal(new Foo()), completedVal(new Foo()), new ImplicitConversion([new ImplicitConversionUnit<>(Foo, String, {
                    '11'
                })] as Set)),
                CompareOperatorExpressionNode.greaterThen(completedVal(new Foo()), completedVal(new Foo()), new ImplicitConversion([new ImplicitConversionUnit<>(Foo, String, {
                    '11'
                })] as Set)),
                CompareOperatorExpressionNode.lowerOrEqual(completedVal(new Foo()), completedVal(new Foo()), new ImplicitConversion([new ImplicitConversionUnit<>(Foo, String, {
                    '11'
                })] as Set)),
                CompareOperatorExpressionNode.lowerThen(completedVal(new Foo()), completedVal(new Foo()), new ImplicitConversion([new ImplicitConversionUnit<>(Foo, String, {
                    '11'
                })] as Set))
        ]
    }

    private static class Foo {}

    private static ValueExpressionNode completedVal(Object o) {
        new ValueExpressionNode(o);
    }
}
