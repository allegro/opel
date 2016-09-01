package pl.allegro.opbox.opel

import spock.lang.Specification
import spock.lang.Unroll

class MultiplyOperatorExpressionNodeSpec extends Specification {

    @Unroll
    def "should multiply BigDecimals"() {
        given:
        def expressionNode = new MultiplyOperatorExpressionNode(completedVal(left), completedVal(right), new ImplicitConversion())

        expect:
        expressionNode.getValue().join() == result

        where:
        left                   | right                  || result
        BigDecimal.valueOf(5)  | BigDecimal.valueOf(3)  || BigDecimal.valueOf(15)
        BigDecimal.valueOf(0)  | BigDecimal.valueOf(5)  || BigDecimal.valueOf(0)
        BigDecimal.valueOf(5)  | BigDecimal.valueOf(0)  || BigDecimal.valueOf(0)
        BigDecimal.valueOf(-5) | BigDecimal.valueOf(3)  || BigDecimal.valueOf(-15)
        BigDecimal.valueOf(5)  | BigDecimal.valueOf(-3) || BigDecimal.valueOf(-15)
        BigDecimal.valueOf(-5) | BigDecimal.valueOf(-3) || BigDecimal.valueOf(15)
    }

    @Unroll
    def "should convert left and right side and multiply them"() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(String, BigDecimal, { new BigDecimal(it) }))
        conversion.register(new ImplicitConversionUnit<>(Integer, BigDecimal, { BigDecimal.valueOf(it) }))
        def expressionNode = new MultiplyOperatorExpressionNode(completedVal(left), completedVal(right), conversion)

        expect:
        expressionNode.getValue().join() == result

        where:
        left | right || result
        '6'  | '3'   || BigDecimal.valueOf(18)
        '6'  | 3i    || BigDecimal.valueOf(18)
        6i   | '3'   || BigDecimal.valueOf(18)
        6i   | 3i    || BigDecimal.valueOf(18)
    }

    def "should treat nulls as zero"() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(String, BigDecimal, { new BigDecimal(it) }))
        conversion.register(new ImplicitConversionUnit<>(Integer, BigDecimal, { BigDecimal.valueOf(it) }))
        def expressionNode = new MultiplyOperatorExpressionNode(completedVal(left), completedVal(right), conversion)

        expect:
        expressionNode.getValue().join() == result

        where:
        left                  | right                 || result
        null                  | BigDecimal.valueOf(3) || BigDecimal.valueOf(0)
        BigDecimal.valueOf(3) | null                  || BigDecimal.valueOf(0)
        null                  | null                  || BigDecimal.valueOf(0)
    }

    @Unroll
    def "should throw exception when multiply object with no BigDecimal conversion"() {
        given:
        def expressionNode = new MultiplyOperatorExpressionNode(completedVal(left), completedVal(right), new ImplicitConversion())

        when:
        expressionNode.getValue().join()

        then:
        thrown RuntimeException

        where:
        left                  | right
        BigDecimal.valueOf(3) | '3'
        '3'                   | BigDecimal.valueOf(3)
        '3'                   | '3'
    }

    private static ValueExpressionNode completedVal(Object o) {
        new ValueExpressionNode(o);
    }

}
