package pl.allegro.tech.opel;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MapInstantiationExpressionNode implements OpelNode {
    private final PairsListNode pairs;

    public MapInstantiationExpressionNode(PairsListNode pairs) {
        this.pairs = pairs;
    }

    @Override
    public CompletableFuture<Map<Object, Object>> getValue(EvalContext context) {
        return FutureUtil.sequence(pairs.getPairs().stream()
                .map(pair -> pair.getKey().getValue(context)
                        .thenCombine(pair.getValue().getValue(context), this::entry))
                .collect(Collectors.toList()))
                .thenApply(list -> list.stream()
                        .collect(HashMap::new, (hashMap, entry) -> hashMap.put(entry.getKey(), entry.getValue()), HashMap::putAll));
    }

    private AbstractMap.SimpleImmutableEntry entry(Object key, Object value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }
}
