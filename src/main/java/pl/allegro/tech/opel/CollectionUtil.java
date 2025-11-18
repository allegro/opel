package pl.allegro.tech.opel;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionUtil {

    static List<IdentifierExpressionNode> getIdentifiers(Collection<? extends OpelNode> nodes) {
        return nodes.stream().<IdentifierExpressionNode>mapMulti((arg, consumer) -> {
            for (IdentifierExpressionNode identifier : arg.getRequiredIdentifiers()) {
                consumer.accept(identifier);
            }
        }).collect(Collectors.toList());
    }
}
