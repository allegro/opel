package pl.allegro.tech.opel

import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

import static pl.allegro.tech.opel.OpelEngineBuilder.create
import static pl.allegro.tech.opel.TestUtil.constFunctionReturning
import static pl.allegro.tech.opel.TestUtil.functions
import static pl.allegro.tech.opel.TestUtil.identityFunction

class OpelEngineMathIntegrationSpec extends Specification {
    @Unroll
    def 'should evaluate math expression #input'() {
        given:
        def engine = create().build()

        expect:
        engine.eval(input).get() == expResult

        where:
        input        || expResult
        "7"          || 7
        "2+3"        || 5
        "2-3"        || -1
        "2*3"        || 6
        "4/2"        || 2
        "-2+3"       || 1
        "-1*2+5"     || 3
        "-4"         || -4
        "--4"        || 4
        "-4*(2+5)/2" || -14
        "1.2+2.3"    || 3.5
        "1.2+4"      || 5.2
        "4+1.2"      || 5.2
        "5/2"        || 2.5
        "5/3"        || 1.6666666667
        "7+null"     || 7
        "null+7"     || 7
        "null+null"  || 0
    }

    @Unroll
    def 'should throw an exception on subtracting with string (expression #input)'() {
        given:
        def engine = create().build()
        def variables = ["o": CompletableFuture.completedFuture(new Object())]
        def evalContext = EvalContextBuilder.create().withValues(variables).build()

        when:
        engine.eval(input, evalContext).get()

        then:
        def e = thrown(ExecutionException)
        e.cause.message == message

        where:
        input   || message
        "1-'5'" || "Can't subtract BigDecimal with String"
        "'5'-2" || "Can't subtract String with BigDecimal"
        "o-2"   || "Can't subtract Object with BigDecimal"
    }

    @Unroll
    def "should use left side as primary type for sum operator (#input)"() {
        def engine = create()
                .withImplicitConversion(String, BigDecimal, { string -> new BigDecimal(string) })
                .withImplicitConversion(BigDecimal, String, { decimal -> decimal.toPlainString() })
                .build()

        expect:
        engine.eval(input).get() == expResult

        where:
        input         || expResult
        "'abc'+123"   || 'abc123'
        "null+'abc'"  || 'abc'
        "5+null"      || 5
        "null+5"      || 5
        "null+5+6"    || 11
        "5+6+null"    || 11
        "5+'5'"       || 10
        "'5'+5"       || '55'
        "5.2+'5'"     || 10.2
        "'123'+'123'" || '123123'
    }

    @Unroll
    def 'should evaluate logical expression #input'() {
        given:
        def engine = create()
                .withImplicitConversion(String, BigDecimal, { string -> new BigDecimal(string) })
                .withImplicitConversion(BigDecimal, String, { decimal -> decimal.toPlainString() })
                .build()
        def evalContext = EvalContextBuilder.create()
                .withCompletedValue('fun', constFunctionReturning('abc'))
                .withValues(functions())
                .build()

        expect:
        engine.eval(input, evalContext).get() == expResult

        where:
        input                                                    || expResult
        "'123'=='123'"                                           || true
        "'123'!='123'"                                           || false
        "123=='123'"                                             || true
        "'123'==123"                                             || true
        "123==123"                                               || true
        "100+23==123"                                            || true
        "200==(123-23)*2"                                        || true
        "fun('x')=='abc'"                                        || true
        "fun('x')!='cba'"                                        || true
        "7=='ś'"                                                 || false
        "'łąć'=='łą\u0107'"                                      || true
        "5 < 6"                                                  || true
        "5 <= 6"                                                 || true
        "5 <= 5"                                                 || true
        "5 > 3"                                                  || true
        "5 >= 6"                                                 || false
        "5 >= 5"                                                 || true
        "'b' > 'abc'"                                            || true
        "true > false"                                           || true
        "'5' > 7"                                                || false
        "'5' > 2"                                                || true
        "null"                                                   || null
        "(2 == 2) == true"                                       || true
        "true && true"                                           || true
        "false && true"                                          || false
        "false || true"                                          || true
        "true || false"                                          || true
        "false || false"                                         || false
        "1 == 1 || false"                                        || true
        "1 == 1 && 2 == 2"                                       || true
        "1 > 2 || 2 > 1"                                         || true
        "identity(false || true)"                                || true
        "identity(false && true)"                                || false
        "identity(if (false || (true && true)) true else false)" || true
    }

    @Unroll
    def '! should negate boolean expression #input'() {
        given:
        def engine = create()
                .withImplicitConversion(String, BigDecimal, { string -> new BigDecimal(string) })
                .withImplicitConversion(BigDecimal, String, { decimal -> decimal.toPlainString() })
                .build()
        def evalContext = EvalContextBuilder.create()
                .withCompletedValue('value', true)
                .withCompletedValue('fun', constFunctionReturning(false))
                .withCompletedValue('identity', identityFunction())
                .build()

        expect:
        engine.eval(input, evalContext).get() == expResult

        where:
        input                || expResult
        "!true"              || false
        "! true"             || false
        "! !true"            || true
        "!!true"             || true
        "!false"             || true
        "!(true)"            || false
        "!value"             || false
        "!(value)"           || false
        "!fun('')"           || true
        "!(fun(''))"         || true
        "!true || true"      || true
        "!(true || true)"    || false
        "!(true || true)"    || false
        "!(false || !false)" || false
        "identity(!true)"    || false
        "!identity(!true)"   || true
        "true && !false"     || true
    }

    def 'AND operator should have higher priority than OR'() {
        given:
        def engine = create().build()

        expect:
        engine.eval(input).get() == expResult

        where:
        input                     || expResult
        "true || true && false"   || true
        "(true || true) && false" || false
    }

    @Unroll
    def "should evaluate relational operators with higher priority then equality operator (#input)"() {
        given:
        def engine = create().build()

        expect:
        engine.eval(input).get() == expResult

        where:
        input              || expResult
        "8 > 5 == true"    || true
        "true == 8 > 5"    || true
        "true != 8 >= 5"   || false
        "2 != 3 <= 5 == 5" || 2 != (3 <= 5) == 5
    }
}
