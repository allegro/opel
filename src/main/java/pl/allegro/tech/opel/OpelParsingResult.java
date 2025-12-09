package pl.allegro.tech.opel;

import org.parboiled.errors.ErrorUtils;
import org.parboiled.errors.ParseError;
import org.parboiled.support.ParsingResult;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class OpelParsingResult {
    private final ParsingResult<OpelNode> parsingResult;
    private final String expression;
    private final EvalContext embeddedEvalContext;

    OpelParsingResult(String expression, ParsingResult<OpelNode> parsingResult, EvalContext embeddedEvalContext) {
        this.parsingResult = parsingResult;
        this.expression = expression;
        this.embeddedEvalContext = embeddedEvalContext;
    }

    public CompletableFuture<?> eval(EvalContext context) {
        return evalWithFinalContext(EvalContextBuilder.mergeContexts(context, embeddedEvalContext));
    }

    public CompletableFuture<?> eval() {
        return evalWithFinalContext(embeddedEvalContext);
    }

    private CompletableFuture<?> evalWithFinalContext(EvalContext context) {
        return getParsedExpression()
                .map(node -> node.getValue(context))
                .orElseThrow(() -> new OpelException("Expression '" + expression + "' contain's syntax error " + ErrorUtils.printParseErrors(parsingResult)));
    }

    private Optional<OpelNode> getParsedExpression() {
        if (parsingResult.hasErrors()) {
            return Optional.empty();
        }
        return Optional.of(parsingResult.resultValue);
    }

    public boolean isValid() {
        return !parsingResult.hasErrors();
    }

    public List<ParseError> getErrors() {
        return parsingResult.parseErrors;
    }

    public String getExpression() {
        return expression;
    }

    public String getParsingErrorMessage() {
        return ErrorUtils.printParseErrors(parsingResult.parseErrors);
    }

    public void accept(OpelNodeVisitor visitor) {
        parsingResult.resultValue.accept(visitor);
    }
}
