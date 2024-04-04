package pl.allegro.tech.opel;

import org.parboiled.Rule;
import org.parboiled.matchers.StringMatcher;

class LabeledStringMatcher extends StringMatcher {
    public LabeledStringMatcher(Rule[] matchers, char[] characters) {
        super(matchers, characters);
    }

    @Override
    public String getLabel() {
        return String.valueOf(characters);
    }
}
