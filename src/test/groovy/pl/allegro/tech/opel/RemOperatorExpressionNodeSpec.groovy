package pl.allegro.tech.opel

import spock.lang.Specification
import spock.lang.Unroll

class RemOperatorExpressionNodeSpec extends Specification {

    @Unroll
    def "remainder of BigDecimals #left % #right should be equal to #result"() {
        given:
        def expressionNode = new RemOperatorExpressionNode(completedVal(left), completedVal(right), new ImplicitConversion())

        expect:
        expressionNode.getValue(EvalContext.empty()).join() == result

        where:
        left                    | right                  || result
        BigDecimal.valueOf(10)  | BigDecimal.valueOf(3)  || BigDecimal.valueOf(1)
        BigDecimal.valueOf(3)  | BigDecimal.valueOf(5)  || BigDecimal.valueOf(3)
        BigDecimal.valueOf(-5)  | BigDecimal.valueOf(2)  || BigDecimal.valueOf(-1)
        BigDecimal.valueOf(-5)  | BigDecimal.valueOf(-2)  || BigDecimal.valueOf(-1)
        BigDecimal.valueOf(5)  | BigDecimal.valueOf(1)  || BigDecimal.valueOf(0)
        BigDecimal.valueOf(0)  | BigDecimal.valueOf(4)  || BigDecimal.valueOf(0)
        BigDecimal.valueOf(0)  | BigDecimal.valueOf(-3)  || BigDecimal.valueOf(0)
    }

    def "should throw exception when trying to calculate remainder of some value and 0 or null"() {
        given:
        def expressionNode = new RemOperatorExpressionNode(completedVal(left), completedVal(right), new ImplicitConversion())

        when:
        expressionNode.getValue(EvalContext.empty()).join()

        then:
        thrown RuntimeException

        where:
        left                   | right
        BigDecimal.valueOf(10) | BigDecimal.valueOf(0)
        BigDecimal.valueOf(0)  | BigDecimal.valueOf(0)
        BigDecimal.valueOf(10) | null
        BigDecimal.valueOf(0)  | null
        null                   | null
    }

    @Unroll
    def "should convert left and right side and compute remainder"() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(String, BigDecimal, { new BigDecimal(it) }))
        conversion.register(new ImplicitConversionUnit<>(Integer, BigDecimal, { BigDecimal.valueOf(it) }))
        def expressionNode = new RemOperatorExpressionNode(completedVal(left), completedVal(right), conversion)

        expect:
        expressionNode.getValue(EvalContext.empty()).join() == result

        where:
        left | right || result
        '7'  | '3'   || BigDecimal.valueOf(1)
        '8'  | 3i    || BigDecimal.valueOf(2)
        6i   | '5'   || BigDecimal.valueOf(1)
        7i   | 3i    || BigDecimal.valueOf(1)
    }

    def "should treat nulls as zero"() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(String, BigDecimal, { new BigDecimal(it) }))
        conversion.register(new ImplicitConversionUnit<>(Integer, BigDecimal, { BigDecimal.valueOf(it) }))
        def expressionNode = new RemOperatorExpressionNode(completedVal(left), completedVal(right), conversion)

        expect:
        expressionNode.getValue(EvalContext.empty()).join() == result

        where:
        left | right                 || result
        null | BigDecimal.valueOf(3) || BigDecimal.valueOf(0)
    }

    @Unroll
    def "should throw exception when divide object with no BigDecimal conversion"() {
        given:
        def expressionNode = new RemOperatorExpressionNode(completedVal(left), completedVal(right), new ImplicitConversion())

        when:
        expressionNode.getValue(EvalContext.empty()).join()

        then:
        thrown RuntimeException

        where:
        left                  | right
        BigDecimal.valueOf(3) | '3'
        '3'                   | BigDecimal.valueOf(3)
        '3'                   | '3'
    }

    private static LiteralExpressionNode completedVal(Object o) {
        new LiteralExpressionNode(o)
    }
}
