package pl.allegro.opbox.opel;

import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class OpelEngine {
    private final ThreadLocal<OpelParser> parser;
    private final ImplicitConversion implicitConversion;

    public OpelEngine() {
        this(MethodExecutionFilters.ALLOW_ALL);
    }

    public OpelEngine(MethodExecutionFilter methodExecutionFilter) {
        implicitConversion = new ImplicitConversion();
        implicitConversion.registerNumberConversion();
        parser = ThreadLocal.withInitial(() -> Parboiled.createParser(OpelParser.class, methodExecutionFilter, implicitConversion));
    }

    public ExpressionValidationResult validate(String expression) {
        ParsingResult<ExpressionNode> parsingResult = getParsingResult(expression);
        if (parsingResult.hasErrors()) {
            return ExpressionValidationResult.invalid(parsingResult.parseErrors);
        } else {
            return ExpressionValidationResult.valid();
        }
    }

    public OpelParsingResult parse(String expression) {
        return new OpelParsingResult(expression, getParsingResult(expression));
    }

    public CompletableFuture<?> eval(String expression) {
        ParsingResult<ExpressionNode> parsingResult = getParsingResult(expression);
        return parsingResult.resultValue.getValue();
    }

    public CompletableFuture<?> eval(String expression, EvalContext evalContext) {
        ParsingResult<ExpressionNode> parsingResult = getParsingResult(expression);
        return parsingResult.resultValue.getValue(evalContext);
    }

    private ParsingResult<ExpressionNode> getParsingResult(String expression) {
        return new ReportingParseRunner<ExpressionNode>(parser.get().ParsingUnit()).run(expression);
    }

    public <T, R> void registerImplicitConversion(Class<T> from, Class<R> to, Function<T, R> conversion) {
        implicitConversion.register(new ImplicitConversionUnit<>(from, to, conversion));
    }
}
