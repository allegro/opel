package pl.allegro.tech.opel

import spock.lang.Specification
import spock.lang.Unroll

class MinusOperatorExpressionNodeSpec extends Specification {
    @Unroll
    def "should subtract BigDecimals"() {
        given:
        def expressionNode = new MinusOperatorExpressionNode(completedVal(left), completedVal(right), new ImplicitConversion())

        expect:
        expressionNode.getValue().join() == result

        where:
        left | right || result
        5.0  | 5.0   || 0.0
        0.0  | 5.0   || -5.0
        5.0  | 0.0   || 5.0
        5.0  | 3.0   || 2.0
        5.0  | 8.0   || -3.0
        -5.0 | 8.0   || -13.0
        5.0  | -8.0  || 13.0
        -5.0 | -8.0  || 3.0
    }

    def "should treat nulls as zero"() {
        given:
        def expressionNode = new MinusOperatorExpressionNode(completedVal(left), completedVal(right), new ImplicitConversion())

        expect:
        expressionNode.getValue().join() == result

        where:
        left | right || result
        null | null  || 0
        2.0  | null  || 2.0
        null | 2.0   || -2.0
    }

    @Unroll
    def "should convert left and right side and subtract them"() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(String, BigDecimal, { new BigDecimal(it) }))
        conversion.register(new ImplicitConversionUnit<>(Integer, BigDecimal, { BigDecimal.valueOf(it) }))
        def expressionNode = new MinusOperatorExpressionNode(completedVal(left), completedVal(right), conversion)

        expect:
        expressionNode.getValue().join() == result

        where:
        left | right || result
        '6'  | '3'   || 3.0
        '6'  | 3i    || 3.0
        6i   | '3'   || 3.0
        6i   | 3i    || 3.0

    }

    @Unroll
    def "should throw exception when subtracting object with no BigDecimal conversion"() {
        given:
        def expressionNode = new MinusOperatorExpressionNode(completedVal(left), completedVal(right), new ImplicitConversion())

        when:
        expressionNode.getValue().join()

        then:
        thrown RuntimeException

        where:
        left | right
        1.0  | '3'
        '3'  | 1.0
        '3'  | '3'
        '3'  | null
        null | '3'
    }

    private static ValueExpressionNode completedVal(Object o) {
        new ValueExpressionNode(o);
    }
}
