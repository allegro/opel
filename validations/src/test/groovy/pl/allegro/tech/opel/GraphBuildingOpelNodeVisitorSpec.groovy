package pl.allegro.tech.opel

import spock.lang.Specification

class GraphBuildingOpelNodeVisitorSpec extends Specification {

    def "should build graph from Opel nodes"() {
        given:
        def parser = new OpelEngineBuilder()
                .withCompletedValue('CUSTOM_PARAMS', Map.of('vte', 'someValue'))
                .build()

        def result = parser.parse(expression)
        def visitor = new GraphBuildingOpelNodeVisitor(parser)

        when:
        result.accept(visitor)

        then:
        visitor.nodes.size() == nodes

        where:
        expression                                                                                                            || nodes
        'val includes = ["id"]; val a = if (isAuthenticated()) includes.add("watchlist") else includes.addAll([]); includes;' || 24
        'CUSTOM_PARAMS.vte'                                                                                                   || 5
    }
}