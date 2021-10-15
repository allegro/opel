package pl.allegro.tech.opel

import spock.lang.Specification

class FieldAccessExpressionNodeSpec extends Specification {
    def "should access field"() {
        given:
        def node = new FieldAccessExpressionNode(valueNode(['x': 'abc']), new IdentifierExpressionNode('x'))

        when:
        def result = node.getValue().get()

        then:
        result == 'abc'
    }

    def "should handle null value"() {
        given:
        def node = new FieldAccessExpressionNode(valueNode(null), new IdentifierExpressionNode('x'))

        when:
        def result = node.getValue().get()

        then:
        result == null
    }

    def "should handle null field"() {
        given:
        def node = new FieldAccessExpressionNode(valueNode(['x': 'abc']), valueNode(null))

        when:
        def result = node.getValue().get()

        then:
        result == null
    }

    private static LiteralExpressionNode valueNode(Object o) {
        new LiteralExpressionNode(o);
    }
}
