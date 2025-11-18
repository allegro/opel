package pl.allegro.tech.opel;

import org.parboiled.buffers.DefaultInputBuffer;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.errors.ParseError;

public class MissingDeclarationError implements ParseError {
    private static final InputBuffer EMPTY_BUFFER = new DefaultInputBuffer("unknown".toCharArray());
    private final String declaration;

    public MissingDeclarationError(String declaration) {
        this.declaration = declaration;
    }

    @Override
    public InputBuffer getInputBuffer() {
        return EMPTY_BUFFER;
    }

    @Override
    public int getStartIndex() {
        return 0;
    }

    @Override
    public int getEndIndex() {
        return 0;
    }

    @Override
    public String getErrorMessage() {
        return "Missing declaration '" + declaration +"'";
    }
}
