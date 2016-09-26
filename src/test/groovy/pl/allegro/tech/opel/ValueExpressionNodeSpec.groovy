package pl.allegro.tech.opel

import spock.lang.Specification

class ValueExpressionNodeSpec extends Specification {
    def "should evaluate to variable from context"() {
        given:
        def valueNode = new ValueExpressionNode('value1')
        def evalContext = EvalContextBuilder.create().withCompletedValue('value1', 'x').build()

        when:
        def result = valueNode.getValue(evalContext).get()

        then:
        result == 'x'
    }
}
