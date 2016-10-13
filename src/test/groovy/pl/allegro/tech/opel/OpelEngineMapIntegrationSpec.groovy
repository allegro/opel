package pl.allegro.tech.opel

import spock.lang.Specification
import spock.lang.Unroll

import static pl.allegro.tech.opel.OpelEngineBuilder.create

class OpelEngineMapIntegrationSpec extends Specification {
    @Unroll
    def 'should access map with [] notation'() {
        given:
        def engine = create()
                .withCompletedValue('aMap', ['a' : 'x', 'b' : 'y', 'c' : 'z', (false) : 'xyz'])
                .build()

        expect:
        engine.eval(input).get() == expResult

        where:
        input                   || expResult
        "aMap['b']"             || 'y'
        "aMap[true == false]"   || 'xyz'
    }

    @Unroll
    def 'should access map with dot (.) notation'() {
        given:
        def engine = create()
                .withCompletedValue('aMap', ['a' : 'x', 'b' : 'y', 'c' : 'z'])
                .build()

        expect:
        engine.eval(input).get() == expResult

        where:
        input                          || expResult
        "aMap.b"                       || 'y'
    }

    static def fun = { it -> it.get(0).thenApply{x -> x+x} } as OpelAsyncFunction

    @Unroll
    def 'should prefer method call over field access with function as a value (#input)'() {
        given:
        def engine = create()
                .withCompletedValue('aMap', ['get' : fun])
                .build()

        expect:
        engine.eval(input).get() == expResult

        where:
        input                         || expResult
        "aMap.get('get')"             || fun
        "(aMap.get)('get')"           || 'getget'
    }

}
