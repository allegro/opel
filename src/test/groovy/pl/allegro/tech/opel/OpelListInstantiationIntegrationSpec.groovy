package pl.allegro.tech.opel

import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

import static pl.allegro.tech.opel.OpelEngineBuilder.create

class OpelListInstantiationIntegrationSpec extends Specification {
    @Unroll
    def 'should instantiation list in #input'() {
        given:
        def engine = create().build()

        expect:
        engine.eval(input).get() == expResult

        where:
        input                          || expResult
        "[]"                           || []
        "['a']"                        || ['a']
        "['a', 'b']"                   || ['a', 'b']
        "['a', 'b', 'c']"              || ['a', 'b', 'c']
        "['a', 2, 'c']"                || ['a', 2, 'c']
        "['a', 2, 'c'].size()"         || 3
        "val x = ['a', 'b']; x.get(0)" || 'a'
        "val x = 1; [x, x, 2]"         || [1, 1, 2]
    }

}
