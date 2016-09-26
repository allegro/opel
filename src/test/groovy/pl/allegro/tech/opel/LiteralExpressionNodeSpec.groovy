package pl.allegro.tech.opel

import spock.lang.Specification

class LiteralExpressionNodeSpec extends Specification {

    def object = new Object()

    def "should evaluate to value"() {
        given:
        def valueNode = new LiteralExpressionNode(object)

        when:
        def result = valueNode.getValue().get()

        then:
        result == object
    }
}
