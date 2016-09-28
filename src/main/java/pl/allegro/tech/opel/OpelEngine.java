package pl.allegro.tech.opel;

import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.util.concurrent.CompletableFuture;

public class OpelEngine {
    private final ThreadLocal<OpelParser> parser;
    private final ImplicitConversion implicitConversion;

    private EvalContext embeddedEvalContext = EvalContext.empty();

    OpelEngine(MethodExecutionFilter methodExecutionFilter, ImplicitConversion implicitConversion, EvalContext embeddedEvalContext) {
        this.embeddedEvalContext = embeddedEvalContext;
        this.implicitConversion = implicitConversion;
        parser = ThreadLocal.withInitial(() -> Parboiled.createParser(OpelParser.class, methodExecutionFilter, this.implicitConversion));
    }

    public ExpressionValidationResult validate(String expression) {
        ParsingResult<OpelNode> parsingResult = getParsingResult(expression);
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
        ParsingResult<OpelNode> parsingResult = getParsingResult(expression);
        return parsingResult.resultValue.getValue(embeddedEvalContext);
    }

    public CompletableFuture<?> eval(String expression, EvalContext evalContext) {
        ParsingResult<OpelNode> parsingResult = getParsingResult(expression);
        return parsingResult.resultValue.getValue(EvalContextBuilder.mergeContexts(evalContext, embeddedEvalContext));
    }

    private ParsingResult<OpelNode> getParsingResult(String expression) {
        return new ReportingParseRunner<OpelNode>(parser.get().ParsingUnit()).run(expression);
    }
}
