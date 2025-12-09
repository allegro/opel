package pl.allegro.tech.opel

import spock.lang.Specification

class DanglingNodesFinderSpec extends Specification {

    def 'should find dangling nodes in the graph'() {
        given:
        def parser = new OpelEngineBuilder()
                .withCompletedValue('CUSTOM_PARAMS', Map.of('vte', 'someValue'))
                .build()

        def parsingResult = parser.parse(expression)
        def visitor = new GraphBuildingOpelNodeVisitor(parser)
        parsingResult.accept(visitor)
        def danglingNodesFinder = new DanglingNodesFinder()

        when:
        def result = danglingNodesFinder.find(visitor.nodes)

        then:
        result.danglingNodes().size() == count

        where:
        expression                                                                                                                                || count
        'val includes = ["id"]; val a = if (isAuthenticated()) includes.add("watchlist") else includes.addAll([]); includes;'                     || 1
        'val includes = ["id"]; val a = if (isAuthenticated()) includes.add("watchlist") else includes.addAll([]); if(a) includes else includes;' || 0
        'CUSTOM_PARAMS.vte'                                                                                                                       || 0
    }
}
