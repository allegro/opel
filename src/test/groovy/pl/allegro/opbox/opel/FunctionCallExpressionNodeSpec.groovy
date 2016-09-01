package pl.allegro.opbox.opel

import spock.lang.Specification

class FunctionCallExpressionNodeSpec extends Specification {
    def "should call function from context"() {
        given:
        def arguments = new ArgumentsListExpressionNode(valueNode('y'), new ArgumentsListExpressionNode(valueNode('x')))
        def functionCallNode = new FunctionCallExpressionNode('f1', arguments)
        def evalContext = EvalContext.Builder.create().withFunctions(functions()).build()

        when:
        def result = functionCallNode.getValue(evalContext).get()

        then:
        result == 'xAndY'

    }

    private static ValueExpressionNode valueNode(Object o) {
        new ValueExpressionNode(o);
    }

    def functions() {
        ['f1': function({ args ->
            args[0] == 'x' && args[1] == 'y' ? 'xAndY' : 'otherThanXAndY'
        })]
    }

    private static function(def body) {
        return { args ->
            FutureUtil.sequence(args).thenApply { completedArgs ->
                body(completedArgs)
            }
        } as OpelAsyncFunction
    }

}
