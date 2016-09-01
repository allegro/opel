package pl.allegro.opbox.opel

import spock.lang.Specification
import spock.lang.Unroll

class DivideOperatorExpressionNodeSpec extends Specification {

    @Unroll
    def "should divide BigDecimals"() {
        given:
        def expressionNode = new DivideOperatorExpressionNode(completedVal(left), completedVal(right), new ImplicitConversion())

        expect:
        expressionNode.getValue().join() == result

        where:
        left                    | right                  || result
        BigDecimal.valueOf(10)  | BigDecimal.valueOf(2)  || BigDecimal.valueOf(5)
        BigDecimal.valueOf(0)   | BigDecimal.valueOf(5)  || BigDecimal.valueOf(0)
        BigDecimal.valueOf(-10) | BigDecimal.valueOf(5)  || BigDecimal.valueOf(-2)
        BigDecimal.valueOf(10)  | BigDecimal.valueOf(-5) || BigDecimal.valueOf(-2)
        BigDecimal.valueOf(-10) | BigDecimal.valueOf(-5) || BigDecimal.valueOf(2)
    }

    def "should throw exception on dividing by zero or null"() {
        given:
        def expressionNode = new DivideOperatorExpressionNode(completedVal(left), completedVal(right), new ImplicitConversion())

        when:
        expressionNode.getValue().join()

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
    def "should convert left and right side and divide them"() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(String, BigDecimal, { new BigDecimal(it) }))
        conversion.register(new ImplicitConversionUnit<>(Integer, BigDecimal, { BigDecimal.valueOf(it) }))
        def expressionNode = new DivideOperatorExpressionNode(completedVal(left), completedVal(right), conversion)

        expect:
        expressionNode.getValue().join() == result

        where:
        left | right || result
        '6'  | '3'   || BigDecimal.valueOf(2)
        '6'  | 3i    || BigDecimal.valueOf(2)
        6i   | '3'   || BigDecimal.valueOf(2)
        6i   | 3i    || BigDecimal.valueOf(2)
    }

    def "should treat nulls as zero"() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(String, BigDecimal, { new BigDecimal(it) }))
        conversion.register(new ImplicitConversionUnit<>(Integer, BigDecimal, { BigDecimal.valueOf(it) }))
        def expressionNode = new DivideOperatorExpressionNode(completedVal(left), completedVal(right), conversion)

        expect:
        expressionNode.getValue().join() == result

        where:
        left | right                 || result
        null | BigDecimal.valueOf(3) || BigDecimal.valueOf(0)
    }

    @Unroll
    def "should throw exception when divide object with no BigDecimal conversion"() {
        given:
        def expressionNode = new DivideOperatorExpressionNode(completedVal(left), completedVal(right), new ImplicitConversion())

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
