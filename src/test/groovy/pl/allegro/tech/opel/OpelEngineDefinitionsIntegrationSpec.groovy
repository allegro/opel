package pl.allegro.tech.opel

import spock.lang.Specification

import static pl.allegro.tech.opel.OpelEngineBuilder.create
import static pl.allegro.tech.opel.TestUtil.functions

class OpelEngineDefinitionsIntegrationSpec extends Specification {

    def "should parse and evaluate expression with definitions (#input)"() {
        given:
        def engine = create().build()
        def evalContext = EvalContextBuilder.create().withFunctions(functions()).build()

        expect:
        engine.parse(input).eval(evalContext).get() == expResult

        where:
        input                                                     || expResult
        "def one=1;one"                                           || 1
        "def two=1+1;two + 1"                                     || 3
        "def two=1+1; def three = two +1; three + 1"              || 4
        "def condition=1==1; if(condition) 5 else 6"              || 5

    }
    def "should end with error when circular definitions are found in #input"() {
        given:
        def engine = create().build()
        def evalContext = EvalContextBuilder.create().withFunctions(functions()).build()

        when:
        engine.parse('def one= two / 2;def two = one + one; one + two').eval(evalContext).get()

        then:
        OpelException ex = thrown()
        ex.message == 'Unknown variable two'
    }

}
