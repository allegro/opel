package pl.allegro.tech.opel

import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

import static OpelEngineBuilder.create
import static pl.allegro.tech.opel.TestUtil.constFunctionReturning
import static pl.allegro.tech.opel.TestUtil.functions

class OpelEngineIntegrationSpec extends Specification {

    @Unroll
    def 'should evaluate string expression #input'() {
        given:
        def engine = create().build()

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
    def 'should evaluate expression ending with ;'() {
        given:
        def engine = create().build()

        expect:
        engine.eval(input).get() == expResult

        where:
        input                || expResult
        "'Guns N\\' Roses';" || "Guns N' Roses"
        "1+2 ;"              || 3
        "1+2  ;   "          || 3
    }

    def 'should return parsing result with errors for redundant ;'() {
        given:
        def engine = create().build()

        expect:
        !engine.parse(input).isValid()

        where:
        input << ["123+'abc';;", ";123+'abc';"]
    }

    def 'should return parsing result with errors for multi line string'() {
        given:
        def engine = create().build()
        def input = """'abc
xyz'"""

        expect:
        !engine.parse(input).isValid()
    }

    @Unroll
    def 'should evaluate expression with whitespaces "#input"'() {
        given:
        def engine = create()
                    .withImplicitConversion(String, BigDecimal, { string -> new BigDecimal(string) })
                    .withImplicitConversion(BigDecimal, String, { decimal -> decimal.toPlainString() })
                    .build()

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
        def engine = create()
                    .withImplicitConversion(String, BigDecimal, { string -> new BigDecimal(string) })
                    .withImplicitConversion(BigDecimal, String, { decimal -> decimal.toPlainString() })
                    .build()

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
    def "should throw an exception when comparing invalid objects in #input expression"() {
        given:
        def variables = ['o': CompletableFuture.completedFuture(new Object())]
        def evalContext = EvalContextBuilder.create().withValues(variables).build()

        when:
        create().build().eval(input, evalContext).get()

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
    def 'should evaluate comparison with function: #input'() {
        given:
        def engine = create().build()
        def evalContext = EvalContextBuilder.create().withValues(functions()).build()

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
        create().build().validate(input).succeed == validationSucceed

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
        ExpressionValidationResult validationResult = create().build().validate("1 ,= 5")

        then:
        validationResult.errorMessage ==
                '''Invalid input ',', expected ' ', '\\t', '\\n', fromStringLiteral or EOI (line 1, pos 3):
                  |1 ,= 5
                  |  ^\n'''.stripMargin()
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

    @Unroll
    def "should access true and false native variables without registration in '#input'"() {
        given:
        def engine = create().build()

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

    @Unroll
    def "should calculate if expression (#input)"() {
        given:
        def engine = create().build()

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
        "(if (true) 'a' else 'b').length()"                            || 1
    }

    def 'should calculate only left value when condition result result is true'() {
        given:
        def engine = create().build()
        def counter1 = 0;
        def counter2 = 0;
        def functions = [
                'one'        : CompletableFuture.completedFuture((OpelAsyncFunction<?>) { CompletableFuture.completedFuture(++counter1) }),
                'twoArgsFunc': CompletableFuture.completedFuture((OpelAsyncFunction<?>) { CompletableFuture.completedFuture(--counter2) })
        ];
        def evalContext = EvalContextBuilder.create().withValues(functions).build()

        when:
        def result = engine.eval("if (true) one() else twoArgsFunc()", evalContext).get()

        then:
        result == 1
        counter1 == 1
        counter2 == 0
    }
}
