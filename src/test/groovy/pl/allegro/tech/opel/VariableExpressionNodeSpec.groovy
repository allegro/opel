package pl.allegro.tech.opel

import spock.lang.Specification

class VariableExpressionNodeSpec extends Specification {
    def "should evaluate to variable from context"() {
        given:
        def variableNode = new VariableExpressionNode('variable1')
        def evalContext = EvalContext.Builder.create().withCompletedVariable('variable1', 'x').build()

        when:
        def result = variableNode.getValue(evalContext).get()

        then:
        result == 'x'
    }
}
