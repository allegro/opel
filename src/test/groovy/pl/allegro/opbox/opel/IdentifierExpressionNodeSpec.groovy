package pl.allegro.opbox.opel

import spock.lang.Specification

class IdentifierExpressionNodeSpec extends Specification {
    def "should evaluate to identifier string"() {
        given:
        def identifierNode = new IdentifierExpressionNode('identifier1')

        when:
        def result = identifierNode.getValue().get()

        then:
        result == 'identifier1'
    }
}
