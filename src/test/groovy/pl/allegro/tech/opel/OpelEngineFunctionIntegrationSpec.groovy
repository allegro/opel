package pl.allegro.tech.opel

import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.ExecutionException

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
        input                                   || expResult
        "val f = () -> 3; f()"                  || 3
        "val square = x -> x * x; square(2)"    || 4
        "val square = (x) -> x * x; square(2)"  || 4
        "val a = 3; val f = x -> a*x*x; f(2)"   || 12
        "val f = (x, y) -> x*x + y*y; f(2, 3)"  || 13
        "(x, y) -> {x*x + y*y}(1, 2)"           || 5
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
                    };
                    f(1,3)""",
                """
                    val x = 3;
                    val f = (a, b) -> {
                        val x = 2;
                        a*x*x + b*x
                    };
                    f(1,3)""",
                """
                    val x = 3;
                    val f = (a, b) -> {
                        val y = a*x*x;
                        y + b*x
                    };
                    f(1,3)"""
        ]
        expResult << [10, 10, 18]
    }

    @Unroll
    def 'should create higher-order function and call it'() {
        given:
        def engine = create().build()

        expect:
        engine.eval(input).get() == expResult

        where:
        input << ["""
                    val f = a -> {
                        b -> {a*2+b}
                    };
                    f(2)(3)
                """,
                """
                    val f = a -> {
                        b -> {
                            c -> a*a*a + b*b + c
                        }
                    };
                    f(1)(2)(3)
                """,
        ]
        expResult << [7, 8]
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
        "val a = 2; x -> a * x"                                  || [completedFuture(2)]                     | 4
        "x -> x * 3"                                             || [completedFuture(3)]                     | 9
        "(x) -> x * 3"                                           || [completedFuture(3)]                     | 9
        "val f = x -> x * 3; f"                                  || [completedFuture(4)]                     | 12
        "val f = (x, y) -> x*x + y*y*y; f"                       || [completedFuture(1), completedFuture(2)] | 9
        "val f = (x, y) -> x*x + y*y*y; val g = x -> f(x, 2); g" || [completedFuture(3)]                     | 17
    }

    def 'should create higher-order function and return inner function'() {
        given:
        def engine = create().build()
        def input = """
                    val f = a -> {
                        b -> {a*2+b}
                    };
                    f(2)
                """

        when:
        def function = engine.eval(input).get()

        then:
        function instanceof OpelAsyncFunction
        function.apply([completedFuture(3)]).get() == 7
    }

    @Unroll
    def "should failed when missing arguments (#input)"() {
        given:
        def engine = create().build()

        when:
        engine.eval(input).get()

        then:
        def ex = thrown ExecutionException
        ex.cause instanceof OpelException

        where:
        input << [
                "val fun = x -> x*x; fun()",
                "val fun = (x, y) -> x*y; fun(3)"]
    }

    @Unroll
    def "should ignore useless arguments (#input)"() {
        given:
        def engine = create().build()

        expect:
        engine.eval(input).get() == expResult

        where:
        input                                      || expResult
        "val fun = () -> 1; fun(2, 3)"             || 1
        "val fun = x -> x*x; fun(2, 3)"            || 4
        "val fun = (x, y) -> x*y; fun(2, 3, 4, 5)" || 6
    }

    def 'should avoid access to val defined in function outside its'() {
        given:
        def engine = create().build()

        when:
        engine.eval(input).get()

        then:
        thrown OpelException

        where:
        input << ["""
                    val fun = (a) -> {
                        val x = 3;
                        a * 3
                    };
                    x
                    """
        ]
    }

    @Unroll
    def "should call function on expression which return function"() {
        given:
        def engine = create().build()
        def input = """
            val foo = x -> x*x;
            val bar = x -> -x;
            (if (x > 0) foo else bar)(x)
            """
        def context = EvalContextBuilder.create().withCompletedValue('x', x).build()

        expect:
        engine.eval(input, context).get() == expResult

        where:
        x  || expResult
        0  || 0
        -2 || 2
        2  || 4
    }

    def "should isolate val declaration scope"() {
        given:
        def engine = create().build()

        expect:
        engine.eval(input).get() == expResult

        where:
        input << ["""
                    val b = 5;
                    val f = a -> {
                        val b = 10;
                        a*b
                    };
                    f(2)
                """,
                  """
                    val b = 5;
                    val f = a -> {
                        val b = 10;
                        a*b
                    };
                    f(b)
                """,
        ]
        expResult << [20, 50]

    }

}
