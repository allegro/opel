package pl.allegro.tech.opel

import spock.lang.Specification

import static pl.allegro.tech.opel.OpelEngineBuilder.create

class OpelEngineCachingIntegrationSpec extends Specification {

    def 'should reuse nodes on parse'() {
        given:
        def engine = create().build()
        def literal = engine.parse("'test'")
        def condition = engine.parse("if ('test' == 'test') 'yes' else 'no'")

        def nodes = engine.parser.get().nodeFactory.nodeCache.map.values()*.get()
                .collect { it.getClass() }
                .countBy { it }
                .findAll { [LiteralExpressionNode, EqualOperatorExpressionNode,IfExpressionNode].contains(it.key) }

        expect:
        nodes == [
            (LiteralExpressionNode): 3,
            (EqualOperatorExpressionNode) : 1,
            (IfExpressionNode)     : 1
        ]

        and:
        getExpressionNode(literal).is(getIfConditionNode(condition).left())
        getExpressionNode(literal).is(getIfConditionNode(condition).right())
    }

    def 'should remove nodes from cache after gc'() {
        given:
        def engine = create().build()
        def node1 = engine.parse("'test'")
        def node2 = engine.parse("if ('test' == 'test') 'yes' else 'no'")
        def numberOfNodesBeforeGc = engine.parser.get().nodeFactory.nodeCache.map.size()

        when:
        node1 = null
        node2 = null
        System.gc()
        Thread.sleep(100) // let the gc do its work

        then:
        engine.parser.get().nodeFactory.nodeCache.map.size() < numberOfNodesBeforeGc
    }

    OpelNode getExpressionNode(OpelParsingResult parsingResult) {
        return parsingResult.parsingResult.parseTreeRoot.value.expression
    }

    BinaryOperationExpressionNode getIfConditionNode(OpelParsingResult parsingResult) {
        return (BinaryOperationExpressionNode)((IfExpressionNode) getExpressionNode(parsingResult)).condition
    }
}
