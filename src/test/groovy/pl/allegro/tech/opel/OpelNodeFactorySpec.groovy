package pl.allegro.tech.opel

import spock.lang.Specification

class OpelNodeFactorySpec extends Specification {

    def 'should create nodes reusing already created ones'() {
        given:
        ImplicitConversion conversion = new ImplicitConversion()
        conversion.registerNumberConversion()
        def factory = new OpelNodeFactory(conversion, MethodExecutionFilters.ALLOW_ALL)

        when:
        def node1 = factory.literalNode(0)
        def node2 = factory.literalNode(0)
        def node3 = factory.literalNode("")

        then:
        node1.is(node2)
        !node1.is(node3)
    }


}
