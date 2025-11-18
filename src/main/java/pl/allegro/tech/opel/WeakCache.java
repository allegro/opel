package pl.allegro.tech.opel;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Supplier;

class WeakCache<V> {
    private final Map<CacheKey, WeakReference<V>> map = new WeakHashMap<>();
    private final Map<WeakReference<V>, CacheKey> keyMap = new WeakHashMap<>();
    private final ReferenceQueue<V> queue = new ReferenceQueue<>();

    V get(Class<?> nodeType, Object[] arguments, Supplier<V> valueSupplier) {
        cleanUp();
        var key = new CacheKey(nodeType, arguments);
        var valueReference = map.get(key);
        if (valueReference != null) {
            return valueReference.get();
        }

        var value = valueSupplier.get();
        var weakReference = new WeakReference<>(value, queue);
        map.put(key, weakReference);
        keyMap.put(weakReference, key);
        return value;
    }

    private void cleanUp() {
        Reference<? extends V> weakReference;
        while ((weakReference = queue.poll()) != null) {
            var key = keyMap.remove(weakReference);
            if (key != null) {
                map.remove(key);
            }
        }
    }

    private static final class CacheKey {
        private final Class<?> nodeType;
        private final WeakArgument[] arguments;

        private CacheKey(Class<?> nodeType, Object[] arguments) {
            this.nodeType = nodeType;
            this.arguments = new WeakArgument[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                this.arguments[i] = new WeakArgument(arguments[i]);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return Objects.equals(nodeType, cacheKey.nodeType) &&
                    Arrays.deepEquals(arguments, cacheKey.arguments);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nodeType, Arrays.deepHashCode(arguments));
        }

        @Override
        public String toString() {
            return "CacheKey[" +
                    "nodeType=" + nodeType + ", " +
                    "arguments=" + arguments + ']';
        }

        private static final class WeakArgument extends WeakReference<Object> {

            public WeakArgument(Object referent) {
                super(referent);
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (!(obj instanceof WeakArgument)) return false;
                Object thisReferent = this.get();
                Object thatReferent = ((WeakArgument) obj).get();
                return Objects.equals(thisReferent, thatReferent);
            }

            @Override
            public int hashCode() {
                Object referent = this.get();
                return referent != null ? referent.hashCode() : 0;
            }
        }
    }
}