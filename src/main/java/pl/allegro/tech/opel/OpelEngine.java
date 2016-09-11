package pl.allegro.tech.opel;

import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class OpelEngine {
    private final ThreadLocal<OpelParser> parser;
    private final ImplicitConversion implicitConversion;

    private final Map<String, OpelAsyncFunction<?>> embeddedFunctions = new HashMap<>();
    private final Map<String, CompletableFuture<Object>> embeddedVariables = new HashMap<>();
    private EvalContext embeddedEvalContext = EvalContext.empty();

    private OpelEngine() {
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
        return new OpelParsingResult(expression, getParsingResult(expression), embeddedEvalContext);
    }

    public CompletableFuture<?> eval(String expression) {
        ParsingResult<ExpressionNode> parsingResult = getParsingResult(expression);
        return parsingResult.resultValue.getValue(embeddedEvalContext);
    }

    public CompletableFuture<?> eval(String expression, EvalContext evalContext) {
        ParsingResult<ExpressionNode> parsingResult = getParsingResult(expression);
        return parsingResult.resultValue.getValue(EvalContext.Builder.mergeContexts(evalContext, embeddedEvalContext));
    }

    private ParsingResult<ExpressionNode> getParsingResult(String expression) {
        return new ReportingParseRunner<ExpressionNode>(parser.get().ParsingUnit()).run(expression);
    }

    public void registerFunction(String functionName, OpelAsyncFunction<?> function) {
        embeddedFunctions.put(functionName, function);
        updateEmbeddedEvalContext();
    }

    public void registerVariable(String variableName, CompletableFuture<Object> value) {
        embeddedVariables.put(variableName, value);
        updateEmbeddedEvalContext();
    }

    public void registerCompletedVariable(String variableName, Object value) {
        embeddedVariables.put(variableName, CompletableFuture.completedFuture(value));
        updateEmbeddedEvalContext();
    }

    public <T, R> void registerImplicitConversion(Class<T> from, Class<R> to, Function<T, R> conversion) {
        implicitConversion.register(new ImplicitConversionUnit<>(from, to, conversion));
    }

    private void updateEmbeddedEvalContext() {
        embeddedEvalContext = EvalContext.Builder.create().withFunctions(embeddedFunctions).withVariables(embeddedVariables).build();
    }
}
