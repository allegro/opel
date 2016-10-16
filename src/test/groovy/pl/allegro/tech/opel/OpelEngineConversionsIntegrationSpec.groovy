package pl.allegro.tech.opel

import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

import static pl.allegro.tech.opel.OpelEngineBuilder.create

class OpelEngineConversionsIntegrationSpec extends Specification {
    @Unroll
    def 'should convert to number for subtracting operator using implicit conversion (expression #input)'() {
        given:
        def engine = create()
                .withImplicitConversion(String, BigDecimal, { string -> new BigDecimal(string) })
                .withImplicitConversion(BigDecimal, String, { decimal -> decimal.toPlainString() })
                .build()

        expect:
        engine.eval(input).get() == expResult

        where:
        input       || expResult
        "1-'5'"     || -4
        "'5'-2"     || 3
        "'5'-'2'"   || 3
        "null-'2'"  || -2
        "2-null"    || 2
        "null-null" || 0
        "1-'-5'"    || 6
    }

    @Unroll
    def "should convert to BigDecimal regardless of whether the left type form  multiplying & dividing operator (#input)"() {
        def engine = create()
                .withImplicitConversion(String, BigDecimal, { string -> new BigDecimal(string) })
                .withImplicitConversion(BigDecimal, String, { decimal -> decimal.toPlainString() })
                .build()

        expect:
        engine.eval(input).get() == expResult

        where:
        input       || expResult
        "4*'5'"     || 20
        "'4'*5"     || 20
        "'4'*'5'"   || 20
        "null*4"    || 0
        "4*null"    || 0
        "null*null" || 0
        "10/'5'"    || 2
        "'10'/5"    || 2
        "'10'/'5'"  || 2
        "null/4"    || 0
    }


    def "should convert string to number in comparison when one way conversion from string to number is registered"() {
        given:
        def engine = create()
                .withImplicitConversion(String, BigDecimal, { string -> new BigDecimal(string) })
                .build()

        expect:
        engine.eval(input).get() == expResult

        where:
        input          || expResult
        " '55' <  7  " || false
        "  55  > '7' " || true
    }

    def "should register conversions in multithreaded environment"() {
        def engine = create()
                .withImplicitConversion(String, RichString, { string -> new RichString(string) })
                .build()
        def executor = Executors.newSingleThreadExecutor()

        when:
        def result = new CompletableFuture<>();
        executor.submit( {
            try {
                result.complete(engine.eval(input).get() == expResult)
            } catch (Exception e) {
                result.completeExceptionally(e)
            }
        } )

        then:
        result.get() == true

        where:
        input          || expResult
        "'opel'.rev()" || "lepo"
        "'abc'.rev()"  || "cba"
    }

    def "should use left site as primary type for sum operator and throw exception when conversion failed"() {
        def engine = create()
                .withImplicitConversion(String, BigDecimal, { string -> new BigDecimal(string) })
                .withImplicitConversion(BigDecimal, String, { decimal -> decimal.toPlainString() })
                .build()
        def variables = ['o': CompletableFuture.completedFuture(new Object())]

        when:
        engine.eval(input, EvalContext.fromMaps(variables, [:])).get()

        then:
        thrown Exception

        where:
        input << ["123+'abc'"]
    }

    @Unroll
    def "should firstly convert right argument when comparing objects in #input"() {
        given:
        def engine = create()
                .withImplicitConversion(String, BigDecimal, { string -> new BigDecimal(string) })
                .withImplicitConversion(BigDecimal, String, { decimal -> decimal.toPlainString() })
                .build()

        expect:
        engine.eval(input).get() == expResult

        where:
        input          || expResult
        " '55' <  7  " || true //compare strings
        "  55  > '7' " || true //compare numbers
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
