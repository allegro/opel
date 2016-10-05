package pl.allegro.tech.opel;

import org.openjdk.jmh.annotations.Benchmark;

public class OpelEngineBanchmark {

    private static final OpelEngine engine = OpelEngineBuilder.create().build();
    private static final String SIMPLE_EXPRESSION = "2 + 2 * 2";
    private static final String COMPLEX_EXPRESSION = "val x = 'some string'; val y = x.length() / 10; if (x > 4) x.toLowerCase else 'lorem ipsum'";
    private static final OpelParsingResult PARSED_SIMPLE_EXPRESSION = engine.parse(SIMPLE_EXPRESSION);
    private static final OpelParsingResult PARSED_COMPLEX_EXPRESSION = engine.parse(COMPLEX_EXPRESSION);

    @Benchmark
    public void validateSimpleExpression() {
        engine.eval(SIMPLE_EXPRESSION);
    }

    @Benchmark
    public void validateComplexExpression() {
        engine.eval(COMPLEX_EXPRESSION);
    }

    @Benchmark
    public void validateSimpleParsedExpression() {
        PARSED_SIMPLE_EXPRESSION.eval();
    }

    @Benchmark
    public void validateComplexParsedExpression() {
        PARSED_COMPLEX_EXPRESSION.eval();
    }

}
