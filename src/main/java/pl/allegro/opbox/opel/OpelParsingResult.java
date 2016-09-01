package pl.allegro.opbox.opel;

import org.parboiled.errors.ErrorUtils;
import org.parboiled.errors.ParseError;
import org.parboiled.support.ParsingResult;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class OpelParsingResult {
    private final ParsingResult<ExpressionNode> parsingResult;
    private final String expression;

    OpelParsingResult(String expression, ParsingResult<ExpressionNode> parsingResult) {
        this.parsingResult = parsingResult;
        this.expression = expression;
    }

    public CompletableFuture<?> eval(EvalContext context) {
        return getParsedExpression()
                .map(node -> node.getValue(context))
                .orElseThrow(() -> new OpelException("Expression '" + expression + "' contain's syntax error"));
    }

    private Optional<ExpressionNode> getParsedExpression() {
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
}
