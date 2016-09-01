package pl.allegro.opbox.opel

import spock.lang.Specification

class MapAccessExpressionNodeSpec extends Specification {
    def "should access map field"() {
        given:
        def node = new MapAccessExpressionNode(valueNode(['x': 'abc']), new IdentifierExpressionNode('x'))

        when:
        def result = node.getValue().get()

        then:
        result == 'abc'
    }

    def "should access list element"() {
        given:
        def node = new MapAccessExpressionNode(valueNode(['a', 'b', 'c']), valueNode(BigDecimal.valueOf(1)))

        when:
        def result = node.getValue().get()

        then:
        result == 'b'
    }

    private static ValueExpressionNode valueNode(Object o) {
        new ValueExpressionNode(o);
    }

}
