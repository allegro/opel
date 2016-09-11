package pl.allegro.tech.opel

import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

class OpelEngineSpec extends Specification {

    @Unroll
    def 'should evaluate math expression #input'() {
        given:
        def engine = new OpelEngine()

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
        def engine = new OpelEngine()
        def variables = ["o": CompletableFuture.completedFuture(new Object())]
        def evalContext = EvalContext.Builder.create().withVariables(variables).build()

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
    def 'should convert to number for subtracting operator using implicit conversion (expression #input)'() {
        given:
        def engine = new OpelEngine()
        engine.registerImplicitConversion(String, BigDecimal, { string -> new BigDecimal(string) })
        engine.registerImplicitConversion(BigDecimal, String, { decimal -> decimal.toPlainString() })

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
    def 'should evaluate string expression #input'() {
        given:
        def engine = new OpelEngine()

        expect:
        engine.eval(input).get() == expResult

        where:
        input               || expResult
        "'Guns N\\' Roses'" || "Guns N' Roses"
        "'AC\\\\DC'"        || "AC\\DC"
        "'abc'"             || "abc"
        "'abc'"             || "abc"
        "'łąć'"             || "łąć"
        "'abc'+'xyz'"       || 'abcxyz'
    }

    @Unroll
    def "should use left side as primary type for sum operator (#input)"() {
        def engine = new OpelEngine()
        engine.registerImplicitConversion(String, BigDecimal, { string -> new BigDecimal(string) })
        engine.registerImplicitConversion(BigDecimal, String, { decimal -> decimal.toPlainString() })

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
    def "should convert to BigDecimal regardless of whether the left type form  multiplying & dividing operator (#input)"() {
        def engine = new OpelEngine()
        engine.registerImplicitConversion(String, BigDecimal, { string -> new BigDecimal(string) })
        engine.registerImplicitConversion(BigDecimal, String, { decimal -> decimal.toPlainString() })

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

    def "should use left site as primary type for sum operator and throw exception when conversion failed"() {
        def engine = new OpelEngine()
        engine.registerImplicitConversion(String, BigDecimal, { string -> new BigDecimal(string) })
        engine.registerImplicitConversion(BigDecimal, String, { decimal -> decimal.toPlainString() })
        def variables = ['o': CompletableFuture.completedFuture(new Object())]

        when:
        engine.eval(input, EvalContext.fromMaps(variables, [:])).get()

        then:
        thrown Exception

        where:
        input << ["123+'abc'"]
    }

    def 'should return parsing result with errors for multi line string'() {
        given:
        def engine = new OpelEngine()
        def input = """'abc
xyz'"""

        expect:
        !engine.parse(input).isValid()
    }

    @Unroll
    def 'should evaluate expression with whitespaces "#input"'() {
        given:
        def engine = new OpelEngine()
        engine.registerImplicitConversion(String, BigDecimal, { string -> new BigDecimal(string) })
        engine.registerImplicitConversion(BigDecimal, String, { decimal -> decimal.toPlainString() })

        expect:
        engine.eval(input).get() == expResult

        where:
        input               || expResult
        "2 +3"              || 5
        "2- 3"              || -1
        "2 * 3"             || 6
        "4 /   2"           || 2
        "-2 +3"             || 1
        "-2 +3  "           || 1
        "-2 +3 \t"          || 1
        "-2 +3 \t\n"        || 1
        "-1 * \t \n2 +5"    || 3
        "-4* ( 2+ 5 )/ 2"   || -14
        "'abc'"             || "abc"
        "'abc' "            || "abc"
        "'abc '"            || "abc "
        "'abc ' "           || "abc "
        "'abc' + 'xyz'"     || 'abcxyz'
        "'abc ' + ' xyz'"   || 'abc  xyz'
        "'abc'+ 123"        || 'abc123'
        "123 == '123'"      || true
        "'123' != 124"      || true
        "'abc' . length() " || 3
    }

    @Unroll
    def 'should evaluate expression which starts with whitespace(s): "#input"'() {
        given:
        def engine = new OpelEngine()
        engine.registerImplicitConversion(String, BigDecimal, { string -> new BigDecimal(string) })
        engine.registerImplicitConversion(BigDecimal, String, { decimal -> decimal.toPlainString() })

        expect:
        engine.eval(input).get() == expResult

        where:
        input                 || expResult
        " -2 +3"              || 1
        "\t  -1 * \t \n2 +5"  || 3
        "\t\n-4* ( 2+ 5 )/ 2" || -14
        " 'abc'"              || "abc"
        "\t 'abc' "           || "abc"
        " 123 == '123'"       || true
        "\t'123' != 124"      || true
    }

    @Unroll
    def 'should evaluate function call expression #input'() {
        given:
        def engine = new OpelEngine()
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
        engine.registerImplicitConversion(String, Integer, { string -> Integer.valueOf(string) })
        engine.registerImplicitConversion(Integer, String, { decimal -> decimal.toString() })
        engine.registerImplicitConversion(BigDecimal, String, { decimal -> decimal.toPlainString() })
        def evalContext = EvalContext.Builder.create().withFunction('ds', function).build()

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

    @Unroll
    def 'should evaluate logical expression #input'() {
        given:
        def engine = new OpelEngine()
        engine.registerImplicitConversion(String, BigDecimal, { string -> new BigDecimal(string) })
        engine.registerImplicitConversion(BigDecimal, String, { decimal -> decimal.toPlainString() })
        def evalContext = EvalContext.Builder.create()
                .withFunction('fun', constFunctionReturning('abc'))
                .withFunctions(functions())
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

    def 'AND operator should have higher priority than OR'() {
        given:
        def engine = new OpelEngine()

        expect:
        engine.eval(input).get() == expResult

        where:
        input                     || expResult
        "true || true && false"   || true
        "(true || true) && false" || false
    }

    @Unroll
    def "should firstly convert right argument when comparing objects in #input"() {
        given:
        def engine = new OpelEngine()
        engine.registerImplicitConversion(String, BigDecimal, { string -> new BigDecimal(string) })
        engine.registerImplicitConversion(BigDecimal, String, { decimal -> decimal.toPlainString() })

        expect:
        engine.eval(input).get() == expResult

        where:
        input          || expResult
        " '55' <  7  " || true //compare strings
        "  55  > '7' " || true //compare numbers
    }

    def "should convert string to number in comparison when one way conversion from string to number is registered"() {
        given:
        def engine = new OpelEngine()
        engine.registerImplicitConversion(String, BigDecimal, { string -> new BigDecimal(string) })

        expect:
        engine.eval(input).get() == expResult

        where:
        input          || expResult
        " '55' <  7  " || false
        "  55  > '7' " || true
    }

    @Unroll
    def "should evaluate relational operators with higher priority then equality operator (#input)"() {
        given:
        def engine = new OpelEngine()

        expect:
        engine.eval(input).get() == expResult

        where:
        input              || expResult
        "8 > 5 == true"    || true
        "true == 8 > 5"    || true
        "true != 8 >= 5"   || false
        "2 != 3 <= 5 == 5" || 2 != (3 <= 5) == 5
    }

    @Unroll
    def "should throw an exception when comparing invalid objects in #input expression"() {
        given:
        def variables = ['o': CompletableFuture.completedFuture(new Object())]
        def evalContext = EvalContext.Builder.create().withVariables(variables).build()

        when:
        new OpelEngine().eval(input, evalContext).get()

        then:
        thrown ExecutionException

        where:
        input << [
                "'abc' < 5", // This can be valid, but implicit conversion has to be defined (see: 'should convert string to number in comparison when one way conversion from string to number is registered' test below)
                "o >= o",
                "o > 5"
        ]
    }

    @Unroll
    def 'should return function value: #input'() {
        given:
        def engine = new OpelEngine()
        def evalContext = EvalContext.Builder.create().withFunctions(functions()).build()

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
    def 'should evaluate comparison with function: #input'() {
        given:
        def engine = new OpelEngine()
        def evalContext = EvalContext.Builder.create().withFunctions(functions()).build()

        expect:
        engine.parse(input).eval(evalContext).get() == expResult

        where:
        input                                         || expResult
        "zero() == 'zero'"                            || true
        "zero () == 'zero'"                           || true
        "zero(   ) == 'zero'"                         || true
        "zero ( ) == 'zero'"                          || true
        "one('x') == 'one'"                           || true
        "twoArgsFunc('x', 'y') == 'xAndY'"            || true
        "twoArgsFunc('x', 'y')=='otherThanXAndY'"     || false
        "twoArgsFunc('a', 'b')=='otherThanXAndY'"     || true
        "oneTwoThree('m', 'n', 'o')=='one two three'" || true
        "fourArgsFunc('m', 'n', 'o', 'p')=='mnop'"    || true
        "(5 == 5) != (5 == 6)"                        || true
        "identity((5 == 5) != (5 == 6))"              || true
        "identity(5 == 5)"                            || true
        "identity(5 != 5)"                            || false
    }

    @Unroll
    def 'validationSucceed should be equal to #validationSucceed for expression: "#input"'() {
        expect:
        new OpelEngine().validate(input).succeed == validationSucceed

        where:
        input                                       || validationSucceed
        "'abc"                                      || false
        "1 ,= 5"                                    || false
        "1 * 5 , 9"                                 || false
        "5 ; 9"                                     || false
        "ds('x').items[0.name + 'abc'"              || false
        "ds('x').items[0[.name"                     || false
        "'abc\\'"                                   || false
        "function('a', 'b', 'c').items[0[.name"     || false
        "function('a', 'b', 'c').items[0).name"     || false
        "function('a', 'b', 'c').items[0,].name"    || false
        "true false"                                || false
        "true null false"                           || false
        "true && || false"                          || false
        "variable.item[0]"                          || true
        "function(10) == true"                      || true
        "3"                                         || true
        "1 + 1"                                     || true
        "1 + 1 * 4"                                 || true
        "'abc' + 'xyz'"                             || true
        "function(10) + 10"                         || true
        "function('a', 'b')"                        || true
        "1 == 10"                                   || true
        "1 != 10"                                   || true
        "function(10).items[0].name"                || true
        "function().items[0].name"                  || true
        "function(10).items[0].prices[0].x"         || true
        "fun1('x').items[0].name"                   || true
        "fun1(1==1)"                                || true
        "identity(1==1)"                            || true
        "identity(1==0)"                            || true
        "false || true && false || (true && false)" || true
    }

    def 'validation should return proper message for invalid expression'() {
        when:
        ExpressionValidationResult validationResult = new OpelEngine().validate("1 ,= 5")

        then:
        validationResult.errorMessage ==
                '''Invalid input ',', expected ' ', '\\t', '\\n', fromStringLiteral or EOI (line 1, pos 3):
                  |1 ,= 5
                  |  ^\n'''.stripMargin()
    }

    def '0-arg function should receive empty list of arguments'() {
        given:
        def engine = new OpelEngine()
        def functionWithoutArgs = { args ->
            if (args != null && args.isEmpty()) {
                return CompletableFuture.completedFuture('Empty list')
            } else {
                return CompletableFuture.completedFuture('Something else')
            }
        } as OpelAsyncFunction
        def evalContext = EvalContext.Builder.create().withFunction('zero', functionWithoutArgs).build()

        expect:
        engine.parse(input).eval(evalContext).get() == expResult

        where:
        input    || expResult
        "zero()" || "Empty list"
    }

    @Unroll
    def 'should call object methods for input #input'() {
        given:
        def engine = new OpelEngine()
        def variables = ["var": CompletableFuture.completedFuture(["a", "b", "c"]), "arg": CompletableFuture.completedFuture("a")]

        engine.registerImplicitConversion(String, BigDecimal, { string -> new BigDecimal(string) })
        engine.registerImplicitConversion(BigDecimal, String, { decimal -> decimal.toPlainString() })

        def evalContext = EvalContext.Builder.create().withVariables(variables).withFunction("fun", constFunctionReturning('Hello, World!')).build()

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

    @Unroll
    def 'should call methods on wrapped object for input #input'() {
        def engine = new OpelEngine()
        engine.registerImplicitConversion(String, RichString, { string -> new RichString(string) })

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

    def "should register conversions in multithreaded environment"() {
        def engine = new OpelEngine()
        engine.registerImplicitConversion(String, RichString, { string -> new RichString(string) })
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

    @Unroll
    def "should access registered variables in #input"() {
        given:
        def variables = ['var': CompletableFuture.completedFuture(["a": [1, 2]])]
        def evalContext = EvalContext.Builder.create().withVariables(variables).build()

        expect:
        new OpelEngine().eval(input, evalContext).get() == expResult

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
        def evalContext = EvalContext.Builder.create().withVariables(variables).build()

        expect:
        new OpelEngine().eval(input, evalContext).get() == expResult

        where:
        input      || expResult
        "var"      || ["a": [1, 2]]
        "var.a"    || [1, 2]
        "var.a[1]" || 2
    }

    @Unroll
    def "should access true and false native variables without registration in '#input'"() {
        given:
        def engine = new OpelEngine()

        expect:
        engine.eval(input).get() == expResult

        where:
        input              || expResult
        "true "            || true
        "true"             || true
        "(true) "          || true
        "false"            || false
        "true==true "      || true
        "true == true"     || true
        "false == false"   || true
        "false == true"    || false
        "false != false"   || false
        "(5 == 5) == true" || true
    }

    def "should be able to register function and variable with the same name"() {
        given:
        def engine = new OpelEngine()
        def vars = ["x": CompletableFuture.completedFuture("var")]
        def evalContext = EvalContext.Builder.create().withVariables(vars).withFunction('x', constFunctionReturning('fun')).build()

        expect:
        engine.eval("x + x()", evalContext).get() == "varfun"
    }

    @Unroll
    def "should calculate if expression (#input)"() {
        given:
        def engine = new OpelEngine()

        expect:
        engine.eval(input).get() == expResult

        where:
        input                                                          || expResult
        "if (true) 'a' else 'b'"                                       || 'a'
        "if (6 > 3) 'a' else 'b'"                                      || 'a'
        "if ('x' == 'x') 'a' else 'b'"                                 || 'a'
        "if (false) 'a' else 'b'"                                      || 'b'
        "if (false) 'a' else 'b'"                                      || 'b'
        "if (null) 'a' else 'b'"                                       || 'b'
        "if (true) 1 == 2 else 2 == 2"                                 || false
        "if (true) null else 'not null'"                               || null
        "if (true) true else false"                                    || true
        "if (true) (null) else 'not null'"                             || null
        "if (1 == 2 || 2 == 2) 'a' else 'b'"                           || 'a'
        "if (1 == 1 && 2 == 2) 'a' else 'b'"                           || 'a'
        "if (1 == 1 && 2 == 3) 'a' else 'b'"                           || 'b'
        "if (1 == 1 && 2 == 3) 1 == 2 && 2 == 2 else 1 == 1 && 2 == 2" || true
        "if (false || false || false || true) 'a' else 'b'"            || 'a'
    }

    def 'should calculate only left value when condition result result is true'() {
        given:
        def engine = new OpelEngine()
        def counter1 = 0;
        def counter2 = 0;
        def functions = [
                'one'        : (OpelAsyncFunction<?>) { CompletableFuture.completedFuture(++counter1) },
                'twoArgsFunc': (OpelAsyncFunction<?>) { CompletableFuture.completedFuture(--counter2) }
        ];
        def evalContext = EvalContext.Builder.create().withFunctions(functions).build()

        when:
        def result = engine.eval("if (true) one() else twoArgsFunc()", evalContext).get()

        then:
        result == 1
        counter1 == 1
        counter2 == 0
    }

    @Unroll
    def "should eval value using provided eval context"() {
        given:
        def evalContext = EvalContext.Builder.create()
                .withCompletedVariable('myVar', 1)
                .withFunction('myFunc', constFunctionReturning(1))
                .build()

        when:
        def eval = new OpelEngine().eval('2+myVar', evalContext).get()

        then:
        eval == 3

        where:
        input << ['2+myVar', '2+myFunc']
    }

    @Unroll
    def "should be able to use variables and functions from parent context"() {
        given:
        def parentContext = EvalContext.Builder.create()
                .withCompletedVariable('myVar', 1)
                .withFunction('myFunc', constFunctionReturning(1))
                .build()

        def evalContext = EvalContext.Builder.create().withParentEvalContext(parentContext).build();

        when:
        def eval = new OpelEngine().eval('2+myVar', evalContext).get()

        then:
        eval == 3

        where:
        input << ['2+myVar', '2+myFunc']
    }

    @Unroll
    def "should be able to override variables and functions from parent context"() {
        given:
        def parentContext = EvalContext.Builder.create()
                .withCompletedVariable('myVar', 1)
                .withFunction('myFunc', constFunctionReturning(1))
                .build()

        def evalContext = EvalContext.Builder.create()
                .withParentEvalContext(parentContext)
                .withCompletedVariable('myVar', 100)
                .withFunction('myFunc', constFunctionReturning(100))
                .build();

        when:
        def result = new OpelEngine().eval('2+myVar', evalContext).get()

        then:
        result == 102

        where:
        input << ['2+myVar', '2+myFunc']
    }

    @Unroll
    def "should evaluate expression with variable and function registered in engine"() {
        given:
        def engine = new OpelEngine()
        engine.registerCompletedVariable('myVar', 202)
        engine.registerFunction('myFunc', constFunctionReturning(103))

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
        def engine = new OpelEngine()
        engine.registerCompletedVariable('myVar', 202)
        engine.registerFunction('myFunc', constFunctionReturning(103))

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
        def engine = new OpelEngine()
        engine.registerCompletedVariable('myVar', 1)
        engine.registerFunction('myFunc', constFunctionReturning(2))

        def evalContext = EvalContext.Builder.create()
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
        def engine = new OpelEngine()
        engine.registerCompletedVariable('myVar', 202)
        engine.registerFunction('myFunc', constFunctionReturning(103))

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
        def engine = new OpelEngine()
        engine.registerCompletedVariable('myVar', 202)
        engine.registerFunction('myFunc', constFunctionReturning(103))

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
        def engine = new OpelEngine()
        engine.registerCompletedVariable('myVar', 1)
        engine.registerFunction('myFunc', constFunctionReturning(2))

        def evalContext = EvalContext.Builder.create()
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


    private static functions() {
        def functionWith2Args = function({ args ->
            args[0] == 'x' && args[1] == 'y' ? 'xAndY' : 'otherThanXAndY'
        })
        def functionWith4Args = function({ args ->
            args.join("")
        })
        def identityFunction = function({ args ->
            args[0]
        })
        return [
                'zero'        : (OpelAsyncFunction<?>) constFunctionReturning('zero'),
                'one'         : (OpelAsyncFunction<?>) constFunctionReturning('one'),
                'twoArgsFunc' : functionWith2Args,
                'oneTwoThree' : constFunctionReturning('one two three'),
                'fourArgsFunc': functionWith4Args,
                'identity'    : identityFunction
        ];
    }

    // Allows to define simple function which returns always 'result' ignoring passed arguments
    private static def constFunctionReturning(def result) {
        function({ args -> result })
    }

    private static function(def body) {
        return { args ->
            FutureUtil.sequence(args).thenApply { completedArgs ->
                body(completedArgs)
            }
        } as OpelAsyncFunction
    }
}
