package pl.allegro.tech.opel

import spock.lang.Specification
import spock.lang.Unroll

import static pl.allegro.tech.opel.OpelEngineBuilder.create

class OpelEngineMapIntegrationSpec extends Specification {
    @Unroll
    def 'should instantiate map defined in #input'() {
        given:
        def engine = create().build()

        expect:
        engine.eval(input).get() == new HashMap(expResult)

        where:
        input                || expResult
        "{}"                 || [:]
        "{'x':2}"            || [x: 2]
        "{x:2}"              || [x: 2]
        "({'x': 2 })"        || [x: 2]
        "{'x': 2, 'y':3 }"   || [x: 2, y: 3]
    }

    @Unroll
    def 'should instantiate map with expression key defined in #input'() {
        given:
        def engine = create().build()

        expect:
        engine.eval(input).get() == new HashMap(expResult)

        where:
        input                              || expResult
        "{(1+1): 2, 'y':3 }"               || [(BigDecimal.valueOf(2)): 2, y: 3]
        "{('x' + 'x'):2}"                  || [xx: 2]
        "{[].size():2}"                    || [0: 2]
        "{([].size()):2}"                  || [0: 2]
        "{(['1'].size()):['1'].size()}"    || [1: 1]
    }

    @Unroll
    def 'should assign map to a value in #input and access an element '() {
        given:
        def engine = create().build()

        expect:
        engine.eval(input).get() == expResult

        where:
        input                                    || expResult
        "val x = {('x' + 'x'):2}; x.xx"          || 2
        "val x = {('x' + 'x'):2}; x.get('xx')"   || 2
        "val x = {('x' + 'x'):2}; x.xx"          || 2
        "val x = {('x' + 'x'):2}; x['xx']"       || 2
    }

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
        "{x:2, y:3}['x']"       || 2
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
        "{x:2, y:5}.x"                 || 2
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
        input                            || expResult
        "aMap.get('get')"                || fun
        "(aMap.get)('get')"              || 'getget'
        "({get: x->x+x}.get('get'))('g')"|| 'gg'
        "({get: x->x+x}.get)('get')"     || 'getget'
    }
}
