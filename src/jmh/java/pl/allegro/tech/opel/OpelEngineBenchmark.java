package pl.allegro.tech.opel;

import org.openjdk.jmh.annotations.Benchmark;

import java.util.concurrent.ExecutionException;

public class OpelEngineBenchmark {

    private static final OpelEngine engine = OpelEngineBuilder.create().build();
    private static final String SIMPLE_EXPRESSION = "2 + 2 * 2 - 10 + 7 + 9";
    private static final String COMPLEX_EXPRESSION = "val x = 'SOME STRING'; val y = x.length() / 10; if (y < 4) x.toLowerCase() else 'lorem ipsum'";
    private static final OpelParsingResult PARSED_SIMPLE_EXPRESSION = engine.parse(SIMPLE_EXPRESSION);
    private static final OpelParsingResult PARSED_COMPLEX_EXPRESSION = engine.parse(COMPLEX_EXPRESSION);

    @Benchmark
    public void validateSimpleExpression() throws ExecutionException, InterruptedException {
        engine.eval(SIMPLE_EXPRESSION).get();
    }

    @Benchmark
    public void validateComplexExpression() throws ExecutionException, InterruptedException {
        engine.eval(COMPLEX_EXPRESSION).get();
    }

    @Benchmark
    public void validateSimpleParsedExpression() throws ExecutionException, InterruptedException {
        PARSED_SIMPLE_EXPRESSION.eval().get();
    }

    @Benchmark
    public void validateComplexParsedExpression() throws ExecutionException, InterruptedException {
        PARSED_COMPLEX_EXPRESSION.eval().get();
    }
}
