package pl.allegro.opbox.opel

import spock.lang.Specification
import spock.lang.Unroll

class SumOperatorExpressionNodeSpec extends Specification {

    def "should return zero when add null to null"() {
        given:
        def expressionNode = new SumOperatorExpressionNode(completedVal(left), completedVal(right), new ImplicitConversion())

        expect:
        expressionNode.getValue().join() == result

        where:
        left | right || result
        null | null  || BigDecimal.valueOf(0)
    }

    @Unroll
    def "should treat null as zero when adding a number"() {
        given:
        def expressionNode = new SumOperatorExpressionNode(completedVal(left), completedVal(right), new ImplicitConversion())

        expect:
        expressionNode.getValue().join() == result

        where:
        left                  | right                 || result
        BigDecimal.valueOf(5) | null                  || BigDecimal.valueOf(5)
        null                  | BigDecimal.valueOf(5) || BigDecimal.valueOf(5)
    }

    @Unroll
    def "should treat null as empty string when adding a string"() {
        given:
        def expressionNode = new SumOperatorExpressionNode(completedVal(left), completedVal(right), new ImplicitConversion())

        expect:
        expressionNode.getValue().join() == result

        where:
        left  | right || result
        'abc' | null  || 'abc'
        null  | 'abc' || 'abc'
    }

    @Unroll
    def "should prefere converting to BigDecimal over String when null is adding"() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(Foo, BigDecimal, { BigDecimal.valueOf(5) }))
        conversion.register(new ImplicitConversionUnit<>(Foo, String, { '10' }))
        def expressionNode = new SumOperatorExpressionNode(completedVal(left), completedVal(right), conversion)

        expect:
        expressionNode.getValue().join() == result

        where:
        left      | right     || result
        new Foo() | null      || BigDecimal.valueOf(5)
        null      | new Foo() || BigDecimal.valueOf(5)
    }

    @Unroll
    def "should converting to String when null is adding and no conversion to BigDecimal registered"() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(Foo, String, { '10' }))
        def expressionNode = new SumOperatorExpressionNode(completedVal(left), completedVal(right), conversion)

        expect:
        expressionNode.getValue().join() == result

        where:
        left      | right     || result
        new Foo() | null      || '10'
        null      | new Foo() || '10'
    }

    def "should sum BigDecimal's without conversion"() {
        given:
        def expressionNode = new SumOperatorExpressionNode(completedVal(BigDecimal.valueOf(5)), completedVal(BigDecimal.valueOf(10)), new ImplicitConversion())

        expect:
        expressionNode.getValue().join() == BigDecimal.valueOf(15)
    }

    def "should concat Strings's without conversion"() {
        given:
        def expressionNode = new SumOperatorExpressionNode(completedVal('a'), completedVal('b'), new ImplicitConversion())

        expect:
        expressionNode.getValue().join() == 'ab'
    }

    @Unroll
    def "should convert argument to BigDecimal if conversion to BigDecimal exist"() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(String, BigDecimal, { new BigDecimal(it) }))
        conversion.register(new ImplicitConversionUnit<>(Foo, BigDecimal, { BigDecimal.valueOf(120) }))
        def expressionNode = new SumOperatorExpressionNode(completedVal(left), completedVal(right), conversion)

        expect:
        expressionNode.getValue().join() == result

        where:
        left                  | right                 || result
        BigDecimal.valueOf(5) | '10'                  || 15
        '10'                  | BigDecimal.valueOf(5) || 15
        new Foo()             | BigDecimal.valueOf(5) || 125
        BigDecimal.valueOf(5) | new Foo()             || 125
        new Foo()             | '10'                  || 130
        '10'                  | new Foo()             || 130
    }

    @Unroll
    def "should convert argument to String if conversion to String exist and BigDecimal conversion missing"() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(BigDecimal, String, { it.toString() }))
        conversion.register(new ImplicitConversionUnit<>(Foo, String, { 'foo' }))
        def expressionNode = new SumOperatorExpressionNode(completedVal(left), completedVal(right), conversion)

        expect:
        expressionNode.getValue().join() == result

        where:
        left                  | right                 || result
        BigDecimal.valueOf(5) | '10'                  || '510'
        '10'                  | BigDecimal.valueOf(5) || '105'
        new Foo()             | '10'                  || 'foo10'
        '10'                  | new Foo()             || '10foo'
        new Foo()             | BigDecimal.valueOf(5) || 'foo5'
        BigDecimal.valueOf(5) | new Foo()             || '5foo'
        new Foo()             | new Foo()             || 'foofoo'
    }

    def "should convert right argument to BigDecimal if left is BigDecimal type"() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(String, BigDecimal, { new BigDecimal(it) }))
        conversion.register(new ImplicitConversionUnit<>(BigDecimal, String, { it.toString() }))
        def expressionNode = new SumOperatorExpressionNode(completedVal(BigDecimal.valueOf(5)), completedVal("10"), conversion)

        expect:
        expressionNode.getValue().join() == BigDecimal.valueOf(15)
    }

    def "should convert right argument to String if left is String type"() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(String, BigDecimal, { new BigDecimal(it) }))
        conversion.register(new ImplicitConversionUnit<>(BigDecimal, String, { it.toString() }))
        def expressionNode = new SumOperatorExpressionNode(completedVal("5"), completedVal(BigDecimal.valueOf(10)), conversion)

        expect:
        expressionNode.getValue().join() == "510"
    }

    def "should prefer conversion to BigDecimal over conversion to String"() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(Foo, BigDecimal, { BigDecimal.valueOf(120) }))
        conversion.register(new ImplicitConversionUnit<>(Foo, String, { 'foo' }))
        def expressionNode = new SumOperatorExpressionNode(completedVal(new Foo()), completedVal(new Foo()), conversion)

        expect:
        expressionNode.getValue().join() == BigDecimal.valueOf(240)
    }


    @Unroll
    def "should use right argument type when left conversion can't be determined clearly"() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(Foo, BigDecimal, { BigDecimal.valueOf(120) }))
        conversion.register(new ImplicitConversionUnit<>(Foo, String, { 'foo' }))
        conversion.register(new ImplicitConversionUnit<>(String, BigDecimal, { new BigDecimal(it) }))
        def expressionNode = new SumOperatorExpressionNode(completedVal(left), completedVal(right), conversion)

        expect:
        expressionNode.getValue().join() == result

        where:
        left      | right                  || result
        new Foo() | '10'                   || 'foo10'
        new Foo() | BigDecimal.valueOf(10) || 130
    }

    def "should throw exception on adding String to BigDecimal when no conversion was registered"() {
        given:
        def expressionNode = new SumOperatorExpressionNode(completedVal("5"), completedVal(BigDecimal.valueOf(10)), new ImplicitConversion())

        when:
        expressionNode.getValue().join()

        then:
        thrown RuntimeException
    }

    @Unroll
    def "should treat Int type as common type (like Foo)"() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(Integer, BigDecimal, { new BigDecimal(it) }))
        conversion.register(new ImplicitConversionUnit<>(String, BigDecimal, { new BigDecimal(it) }))
        conversion.register(new ImplicitConversionUnit<>(Integer, String, { Integer.toString(it) }))
        def expressionNode = new SumOperatorExpressionNode(completedVal(left), completedVal(right), conversion)

        expect:
        expressionNode.getValue().join() == result

        where:
        left                  | right                  || result
        5i                    | '10'                   || '510'
        '5'                   | 10i                    || '510'
        5i                    | BigDecimal.valueOf(10) || 15
        BigDecimal.valueOf(5) | 10i                    || 15
    }

    def "should throw exception on adding type which can't be converted to String or BigDecimal"() {
        given:
        def conversion = new ImplicitConversion()
        def expressionNode = new SumOperatorExpressionNode(completedVal(left), completedVal(right), conversion)

        when:
        expressionNode.getValue().join()

        then:
        thrown RuntimeException

        where:
        left      | right
        new Foo() | 10
        10        | new Foo()
        new Foo() | '10'
        '10'      | new Foo()
        new Foo() | new Foo()
    }

    private static class Foo {}

    private static ValueExpressionNode completedVal(Object o) {
        new ValueExpressionNode(o);
    }
}
