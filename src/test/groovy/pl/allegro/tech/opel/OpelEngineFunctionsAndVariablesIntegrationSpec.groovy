package pl.allegro.tech.opel

import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.CompletableFuture

import static pl.allegro.tech.opel.OpelEngineBuilder.create
import static pl.allegro.tech.opel.TestUtil.constFunctionReturning
import static pl.allegro.tech.opel.TestUtil.functions

class OpelEngineFunctionsAndVariablesIntegrationSpec extends Specification {

    @Unroll
    def 'should return function value: #input'() {
        given:
        def engine = create().build()
        def evalContext = EvalContextBuilder.create().withFunctions(functions()).build()

        expect:
        engine.parse(input).eval(evalContext).get() == expResult

        where:
        input                              || expResult
        "zero()"                           || 'zero'
        "one('x')"                         || 'one'
        "one(1 == 1)"                      || 'one'
        "twoArgsFunc('x', 'y')"            || 'xAndY'
        "twoArgsFunc('x', 'z')"            || 'otherThanXAndY'
        "oneTwoThree('m', 'n', 'o')"       || 'one two three'
        "fourArgsFunc('m', 'n', 'o', 'p')" || 'mnop'
    }


    @Unroll
    def 'should evaluate function call expression #input'() {
        given:
        def engine = create()
                .withImplicitConversion(String, Integer, { string -> Integer.valueOf(string) })
                .withImplicitConversion(Integer, String, { decimal -> decimal.toString() })
                .withImplicitConversion(BigDecimal, String, { decimal -> decimal.toPlainString() })
                .build()
        OpelAsyncFunction function = { args ->
            args.get(0).thenApply({ o ->
                switch (o) {
                    case 7.0: return "abc"
                    case 6.0: return [name: "123"]
                    case 5.0: return [l1: [item: [name: 777]]]
                    case 4.0: return [items: [[name: 666]]]
                    default: return null;
                }
            })
        }

        def evalContext = EvalContextBuilder.create().withFunction('ds', function).build()

        expect:
        engine.parse(input).eval(evalContext).get() == expResult

        where:
        input                                        || expResult
        "ds(7)"                                      || "abc"
        "ds(6).name"                                 || "123"
        "ds(6)['name']"                              || "123"
        "ds(6)"                                      || [name: "123"]
        "ds(5).l1.item.name"                         || 777
        "ds(5).l1['item'].name"                      || 777
        "ds(4).items[0].name"                        || 666
        "ds(4).items[(4-2)*3-6].name"                || 666
        "ds(1+2*3-3).items[0].name"                  || 666
        "ds(4).items[ 0].name"                       || 666
        "ds(4).items[0 ] .name"                      || 666
        "ds(4).items[ 0 ]. name"                     || 666
        "ds(4) .items[ 0 ]. name"                    || 666
        "'abc'+ds(4).items[0].name+'xyz'"            || 'abc666xyz'
        "'chcę kupić '+ds(4).items[0].name+' bułek'" || 'chcę kupić 666 bułek'
        "'abc'+5+5"                                  || "abc55"
        "'abc'+(5+5)"                                || "abc10"
    }

    def '0-arg function should receive empty list of arguments'() {
        given:
        def engine = create().build()
        def functionWithoutArgs = { args ->
            if (args != null && args.isEmpty()) {
                return CompletableFuture.completedFuture('Empty list')
            } else {
                return CompletableFuture.completedFuture('Something else')
            }
        } as OpelAsyncFunction
        def evalContext = EvalContextBuilder.create().withFunction('zero', functionWithoutArgs).build()

        expect:
        engine.parse(input).eval(evalContext).get() == expResult

        where:
        input    || expResult
        "zero()" || "Empty list"
    }

    @Unroll
    def "should access registered variables in #input"() {
        given:
        def variables = ['var': CompletableFuture.completedFuture(["a": [1, 2]])]
        def evalContext = EvalContextBuilder.create().withVariables(variables).build()

        expect:
        create().build().eval(input, evalContext).get() == expResult

        where:
        input      || expResult
        "var"      || ["a": [1, 2]]
        "var.a"    || [1, 2]
        "var.a[1]" || 2
    }

    @Unroll
    def "should access registered future variables in #input"() {
        given:
        def variables = ['var': CompletableFuture.completedFuture(["a": [1, 2]])]
        def evalContext = EvalContextBuilder.create().withVariables(variables).build()

        expect:
        create().build().eval(input, evalContext).get() == expResult

        where:
        input      || expResult
        "var"      || ["a": [1, 2]]
        "var.a"    || [1, 2]
        "var.a[1]" || 2
    }

    def "should be able to register function and variable with the same name"() {
        given:
        def engine = create()
                .withVariable("x", CompletableFuture.completedFuture("var"))
                .withFunction('x', constFunctionReturning('fun'))
                .build()
        expect:
        engine.eval("x + x()").get() == "varfun"
    }

    def "should be able to use context with function and variable with the same name"() {
        given:
        def engine = create().build()
        def vars = ["x": CompletableFuture.completedFuture("var")]
        def evalContext = EvalContextBuilder.create().withVariables(vars).withFunction('x', constFunctionReturning('fun')).build()

        expect:
        engine.eval("x + x()", evalContext).get() == "varfun"
    }

    @Unroll
    def "should eval value using provided eval context"() {
        given:
        def evalContext = EvalContextBuilder.create()
                .withCompletedVariable('myVar', 1)
                .withFunction('myFunc', constFunctionReturning(1))
                .build()

        when:
        def eval = create().build().eval('2+myVar', evalContext).get()

        then:
        eval == 3

        where:
        input << ['2+myVar', '2+myFunc']
    }

    @Unroll
    def "should be able to use variables and functions from external context"() {
        given:
        def externalContext = EvalContextBuilder.create()
                .withCompletedVariable('myVar', 1)
                .withFunction('myFunc', constFunctionReturning(1))
                .build()

        def evalContext = EvalContextBuilder.create().withExternalEvalContext(externalContext).build();

        when:
        def eval = create().build().eval('2+myVar', evalContext).get()

        then:
        eval == 3

        where:
        input << ['2+myVar', '2+myFunc']
    }

    @Unroll
    def "should be able to override variables and functions from external context"() {
        given:
        def externalContext = EvalContextBuilder.create()
                .withCompletedVariable('myVar', 1)
                .withFunction('myFunc', constFunctionReturning(1))
                .build()

        def evalContext = EvalContextBuilder.create()
                .withExternalEvalContext(externalContext)
                .withCompletedVariable('myVar', 100)
                .withFunction('myFunc', constFunctionReturning(100))
                .build();

        when:
        def result = create().build().eval('2+myVar', evalContext).get()

        then:
        result == 102

        where:
        input << ['2+myVar', '2+myFunc']
    }

    @Unroll
    def "should evaluate expression with variable and function registered in engine"() {
        given:
        def engine = create()
                .withFunction('myFunc', constFunctionReturning(103))
                .withCompletedVariable('myVar', 202)
                .build()

        when:
        def value = engine.eval(expression)

        then:
        value.get() == expected

        where:
        expression                          | expected
        '2 + myVar'                         | 204
        '3 + myFunc()'                      | 106
    }

    @Unroll
    def "should evaluate expression with variable and function registered in engine with empty context given"() {
        given:
        def engine = create()
                .withFunction('myFunc', constFunctionReturning(103))
                .withCompletedVariable('myVar', 202)
                .build()

        when:
        def value = engine.eval(expression, EvalContext.empty())

        then:
        value.get() == expected

        where:
        expression                          | expected
        '2 + myVar'                         | 204
        '3 + myFunc()'                      | 106
    }

    @Unroll
    def "should prefere variables and functions from eval context"() {
        given:
        def engine = create()
                .withFunction('myFunc', constFunctionReturning(2))
                .withCompletedVariable('myVar', 1)
                .build()

        def evalContext = EvalContextBuilder.create()
                .withFunction('myFunc', constFunctionReturning(1000))
                .withCompletedVariable('myVar', 2000)
                .build()

        when:
        def value = engine.eval(expression, evalContext)

        then:
        value.get() == expected

        where:
        expression                          | expected
        '2 + myVar'                         | 2002
        '3 + myFunc()'                      | 1003
    }

    @Unroll
    def "should evaluate parsed expression with variable and function registered in engine"() {
        given:
        def engine = create()
                .withFunction('myFunc', constFunctionReturning(103))
                .withCompletedVariable('myVar', 202)
                .build()

        when:
        def value = engine.parse(expression).eval()

        then:
        value.get() == expected

        where:
        expression                          | expected
        '2 + myVar'                         | 204
        '3 + myFunc()'                      | 106
    }

    @Unroll
    def "should evaluate parsed expression with variable and function registered in engine with empty context given"() {
        given:
        def engine = create()
                .withFunction('myFunc', constFunctionReturning(103))
                .withCompletedVariable('myVar', 202)
                .build()

        when:
        def value = engine.parse(expression).eval(EvalContext.empty())

        then:
        value.get() == expected

        where:
        expression                          | expected
        '2 + myVar'                         | 204
        '3 + myFunc()'                      | 106
    }

    @Unroll
    def "should prefere variables and functions from eval context when evaluating parsed expression"() {
        given:
        def engine = create()
                .withCompletedVariable('myVar', 1)
                .withFunction('myFunc', constFunctionReturning(2))
                .build()

        def evalContext = EvalContextBuilder.create()
                .withFunction('myFunc', constFunctionReturning(1000))
                .withCompletedVariable('myVar', 2000)
                .build()

        when:
        def value = engine.parse(expression).eval(evalContext)

        then:
        value.get() == expected

        where:
        expression                          | expected
        '2 + myVar'                         | 2002
        '3 + myFunc()'                      | 1003
    }
}
