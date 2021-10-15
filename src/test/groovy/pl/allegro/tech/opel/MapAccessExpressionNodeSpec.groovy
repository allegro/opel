package pl.allegro.tech.opel

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

    def "should handle null value"() {
        given:
        def node = new MapAccessExpressionNode(valueNode(null), new IdentifierExpressionNode('x'))

        when:
        def result = node.getValue().get()

        then:
        result == null
    }

    def "should handle null field"() {
        given:
        def node = new MapAccessExpressionNode(valueNode(['x': 'abc']), valueNode(null))

        when:
        def result = node.getValue().get()

        then:
        result == null
    }

    private static LiteralExpressionNode valueNode(Object o) {
        new LiteralExpressionNode(o);
    }

}
