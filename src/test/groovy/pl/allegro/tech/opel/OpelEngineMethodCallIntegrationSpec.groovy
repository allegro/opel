package pl.allegro.tech.opel

import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

import static OpelEngineBuilder.create
import static pl.allegro.tech.opel.TestUtil.constFunctionReturning
import static pl.allegro.tech.opel.TestUtil.functions

class OpelEngineMethodCallIntegrationSpec extends Specification {

    @Unroll
    def 'should call object methods for input #input'() {
        given:
        def engine = create()
                .withImplicitConversion(String, BigDecimal, { string -> new BigDecimal(string) })
                .withImplicitConversion(BigDecimal, String, { decimal -> decimal.toPlainString() })
                .build()

        def variables = ["var": CompletableFuture.completedFuture(["a", "b", "c"]), "arg": CompletableFuture.completedFuture("a")]

        def evalContext = EvalContextBuilder.create().withValues(variables).withCompletedValue("fun", constFunctionReturning('Hello, World!')).build()

        expect:
        engine.parse(input).eval(evalContext).get() == expResult

        where:
        input                         || expResult
        "fun().length()"              || 13
        "fun().contains('ello')"      || true
        "fun().charAt(1)"             || 'e'
        "fun().substring(5).length()" || 8
        "'Hello, World!'.length()"    || 13
        "var.contains('a')"           || true
        "var.contains(arg)"           || true
    }

    def "should call method with null value as argument"() {
        given:
        def engine = create().build()

        expect:
        engine.eval("['a', 'b', 'c'].contains(null)").get() == false
    }

    @Unroll
    def 'should call methods on wrapped object for input #input'() {
        def engine = create()
                    .withImplicitConversion(String, RichString, { string -> new RichString(string) })
                    .build()

        expect:
        engine.eval(input).get() == expResult

        where:
        input          || expResult
        "'opel'.rev()" || "lepo"
        "'abc'.rev()"  || "cba"
    }

    public static class RichString {
        private final String delegate;

        RichString(String delegate) {
            this.delegate = delegate
        }

        String rev() {
            return delegate.reverse()
        }
    }
}
