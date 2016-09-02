package pl.allegro.tech.opel;

import org.parboiled.errors.ErrorUtils;
import org.parboiled.errors.ParseError;

import java.util.Collections;
import java.util.List;

public class ExpressionValidationResult {
    private final boolean validationSucceed;
    private final String errorMessage;

    static ExpressionValidationResult valid() {
        return new ExpressionValidationResult(true, Collections.emptyList());
    }

    static ExpressionValidationResult invalid(List<ParseError> errors) {
        return new ExpressionValidationResult(false, errors);
    }

    private ExpressionValidationResult(boolean validationSucceed, List<ParseError> parseErrors) {
        this.validationSucceed = validationSucceed;
        this.errorMessage = ErrorUtils.printParseErrors(parseErrors);
    }

    public boolean isSucceed() {
        return validationSucceed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
