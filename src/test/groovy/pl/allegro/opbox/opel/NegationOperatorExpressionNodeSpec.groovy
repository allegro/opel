package pl.allegro.opbox.opel

import spock.lang.Specification
import spock.lang.Unroll

class NegationOperatorExpressionNodeSpec extends Specification {

    def "should negate null to 0"() {
        given:
        def equalExpressionNode = new NegationOperatorExpressionNode(completedVal(value), new ImplicitConversion())

        expect:
        equalExpressionNode.getValue().join() == result

        where:
        value || result
        null  || BigDecimal.valueOf(0)
    }

    @Unroll
    def "should negate BigDecimal"() {
        given:
        def equalExpressionNode = new NegationOperatorExpressionNode(completedVal(value), new ImplicitConversion())

        expect:
        equalExpressionNode.getValue().join() == result

        where:
        value                  || result
        BigDecimal.valueOf(7)  || BigDecimal.valueOf(-7)
        BigDecimal.valueOf(-7) || BigDecimal.valueOf(7)
        BigDecimal.valueOf(0)  || BigDecimal.valueOf(0)
    }

    @Unroll
    def "should convert to BigDecimal and negate result"() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(String, BigDecimal, { new BigDecimal(it) }))
        def equalExpressionNode = new NegationOperatorExpressionNode(completedVal(value), conversion)

        expect:
        equalExpressionNode.getValue().join() == result

        where:
        value || result
        '7'   || BigDecimal.valueOf(-7)
        '-7'  || BigDecimal.valueOf(7)
        '0'   || BigDecimal.valueOf(0)
    }

    def "should throw exception when conversion to BigDecimal not found"() {
        given:

        def equalExpressionNode = new NegationOperatorExpressionNode(completedVal('7'), new ImplicitConversion())

        when:
        equalExpressionNode.getValue().join()

        then:
        thrown RuntimeException
    }

    def "should throw exception when conversion to BigDecimal failed"() {
        given:
        def conversion = new ImplicitConversion()
        conversion.register(new ImplicitConversionUnit<>(String, BigDecimal, { new BigDecimal(it) }))
        def equalExpressionNode = new NegationOperatorExpressionNode(completedVal('a'), conversion)

        when:
        equalExpressionNode.getValue().join()

        then:
        thrown RuntimeException
    }

    private static ValueExpressionNode completedVal(Object o) {
        new ValueExpressionNode(o);
    }
}
