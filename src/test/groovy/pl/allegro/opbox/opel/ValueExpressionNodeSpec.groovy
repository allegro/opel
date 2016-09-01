package pl.allegro.opbox.opel

import spock.lang.Specification

class ValueExpressionNodeSpec extends Specification {

    def object = new Object()

    def "should evaluate to value"() {
        given:
        def valueNode = new ValueExpressionNode(object)

        when:
        def result = valueNode.getValue().get()

        then:
        result == object
    }
}
