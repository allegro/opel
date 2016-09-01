package pl.allegro.opbox.opel;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.function.Function;

class ImplicitConversionUnit<T, R> {
    private final Class<T> from;
    private final Class<R> to;
    private final Function<T, R> convert;

    public ImplicitConversionUnit(Class<T> from, Class<R> to, Function<T, R> convert) {
        this.from = from;
        this.to = to;
        this.convert = convert;
    }

    R convert(Object object) {
        if (ClassUtils.isAssignable(object.getClass(), from)) {
            return convert.apply((T) object);
        }
        throw new OpelException("Can't convert '" + object.getClass().getSimpleName() + "', expected '" + from.getSimpleName() + "'");
    }

    boolean isApplicable(Class<?> givenType, Class<?> expectedType) {
        return ClassUtils.isAssignable(givenType, from) && ClassUtils.isAssignable(expectedType, to);
    }

    Class<T> getFrom() {
        return from;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ImplicitConversionUnit<?, ?> that = (ImplicitConversionUnit<?, ?>) o;

        return new EqualsBuilder()
                .append(from, that.from)
                .append(to, that.to)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(from)
                .append(to)
                .toHashCode();
    }
}
