package pl.allegro.opbox.opel

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

    private static ValueExpressionNode valueNode(Object o) {
        new ValueExpressionNode(o);
    }
}
