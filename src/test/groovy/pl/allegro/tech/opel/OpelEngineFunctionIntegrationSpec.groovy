package pl.allegro.tech.opel

import spock.lang.Specification
import spock.lang.Unroll

import static java.util.concurrent.CompletableFuture.completedFuture
import static pl.allegro.tech.opel.OpelEngineBuilder.create

class OpelEngineFunctionIntegrationSpec extends Specification {
    @Unroll
    def 'should instantiate function in #input'() {
        given:
        def engine = create().build()

        expect:
        engine.eval(input).get() == expResult

        where:
        input                                         || expResult
        "val f = () -> 3; f()"                        || 3
        "val square = x -> x * x; square(2)"          || 4
        "val square = (x) -> x * x; square(2)"        || 4
        "val a = 3; val f = x -> a*x*x; f(2)"         || 12
        "val f = (x, y) -> x*x + y*y; f(2, 3)"        || 13
    }

    @Unroll
    def 'should instantiate multiline function in #input'() {
        given:
        def engine = create().build()

        expect:
        engine.eval(input).get() == expResult

        where:
        input << [
                """
                    val f = (a, b) -> {
                    val x = 2;
                    a*x*x + b*x
                    }; f(1,3)""",
                """
                    val x = 3; val f = (a, b) -> {
                    val x = 2;
                    a*x*x + b*x
                    }; f(1,3)""",
                """
                    val x = 3 val f = (a, b) -> {
                    a*x*x + b*x
                    }; f(1,3)"""
        ]
        expResult << [7, 7, 12]
    }

    @Unroll
    def 'should return function in #input'() {
        given:
        def engine = create().build()

        when:
        def function = engine.eval(input).get()

        then:
        function instanceof OpelAsyncFunction
        function.apply(args).get() == expResult

        where:
        input                                                    || args                                     | expResult
        "() -> 3"                                                || []                                       | 3
        "val a = 2; x -> a * x"                                  || [completedFuture(2)]                     | 6
        "x -> x * 3"                                             || [completedFuture(3)]                     | 9
        "val f = x -> x * 3; f"                                  || [completedFuture(4)]                     | 12
        "val f = (x, y) -> x*x + y*y*y; f"                       || [completedFuture(1), completedFuture(2)] | 9
        "val f = (x, y) -> x*x + y*y*y; val g = x -> f(x, 2); g" || [completedFuture(3)]                     | 17
    }

}
