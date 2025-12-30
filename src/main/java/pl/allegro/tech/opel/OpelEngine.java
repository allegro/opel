package pl.allegro.tech.opel;

import org.parboiled.Parboiled;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class OpelEngine {
    private final ThreadLocal<OpelParser> parser;
    private final ImplicitConversion implicitConversion;
    private final EvalContext embeddedEvalContext;

    OpelEngine(MethodExecutionFilter methodExecutionFilter, ImplicitConversion implicitConversion, EvalContext embeddedEvalContext) {
        this.embeddedEvalContext = embeddedEvalContext;
        this.implicitConversion = implicitConversion;
        parser = ThreadLocal.withInitial(() -> Parboiled.createParser(OpelParser.class, methodExecutionFilter, this.implicitConversion));
    }

    public ExpressionValidationResult validate(String expression) {
        ParsingResult<OpelNode> parsingResult = getParsingResult(expression);
        var missingDeclarations = findMissingDeclarations(parsingResult);
        if (!missingDeclarations.isEmpty()) {
            var parseErrors = missingDeclarations.stream()
                .map(MissingDeclarationError::new)
                .collect(Collectors.<ParseError>toList());
            return ExpressionValidationResult.invalid(parseErrors);
        }
        if (parsingResult.hasErrors()) {
            return ExpressionValidationResult.invalid(parsingResult.parseErrors);
        } else {
            return ExpressionValidationResult.valid();
        }
    }

    private Set<String> findMissingDeclarations(ParsingResult<OpelNode> parsingResult) {
        if (parsingResult.resultValue instanceof ProgramNode programNode) {
            var identifiers = programNode.getRequiredIdentifiers();
            return identifiers.stream()
                    .map(IdentifierExpressionNode::getIdentifier)
                    .filter(identifier -> !embeddedEvalContext.hasValue(identifier))
                    .collect(Collectors.toSet());
        }
        return Set.of();
    }

    public OpelParsingResult parse(String expression) {
        return new OpelParsingResult(expression, getParsingResult(expression), embeddedEvalContext);
    }

    public CompletableFuture<?> eval(String expression) {
        ParsingResult<OpelNode> parsingResult = getParsingResult(expression);
        if (parsingResult.hasErrors()) {
            throw new OpelException("Error parsing expression: '" + expression + "'" + additionalErrorMsg(parsingResult));
        }
        return parsingResult.resultValue.getValue(embeddedEvalContext);
    }

    private String additionalErrorMsg(ParsingResult<OpelNode> parsingResult) {
        var stringJoiner = new StringJoiner(";", " because of ", "");
        stringJoiner.setEmptyValue("");
        Collector<CharSequence, ?, String> joinerCollector = Collector.of(
            () -> stringJoiner,
            StringJoiner::add,
            StringJoiner::merge,
            StringJoiner::toString
        );
        return parsingResult.parseErrors.stream()
            .map(ErrorUtils::printParseError)
            .filter(it -> it != null)
            .collect(joinerCollector);

    }

    public CompletableFuture<?> eval(String expression, EvalContext evalContext) {
        ParsingResult<OpelNode> parsingResult = getParsingResult(expression);
        return parsingResult.resultValue.getValue(EvalContextBuilder.mergeContexts(evalContext, embeddedEvalContext));
    }

    private ParsingResult<OpelNode> getParsingResult(String expression) {
        return new ReportingParseRunner<OpelNode>(parser.get().ParsingUnit()).run(expression);
    }
}
