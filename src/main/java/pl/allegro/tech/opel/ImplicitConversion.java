package pl.allegro.tech.opel;

import org.apache.commons.lang3.ClassUtils;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

class ImplicitConversion {
    private final Set<ImplicitConversionUnit<?, ?>> implicitConversionUnits;

    public ImplicitConversion() {
        this(new HashSet<>());
    }

    ImplicitConversion(Set<ImplicitConversionUnit<?, ?>> implicitConversionUnits) {
        this.implicitConversionUnits = implicitConversionUnits;
    }

    public void register(ImplicitConversionUnit<?, ?> conversionUnit) {
        implicitConversionUnits.add(conversionUnit);
    }

    void registerNumberConversion() {
        implicitConversionUnits.addAll(numberToBigDecimalConversion());
    }

    public <R> R convert(Object object, Class<R> expectedType) {
        if (object == null && expectedType.equals(Boolean.class)) {
            return (R) Boolean.FALSE;
        }
        if (object == null || ClassUtils.isAssignable(object.getClass(), expectedType)) {
            return (R) object;
        }
        Class<?> givenType = object.getClass();
        return implicitConversionUnits.stream()
                .filter(conversionUnit -> conversionUnit.isApplicable(givenType, expectedType))
                .findFirst()
                .map(it -> (R) it.convert(object))
                .orElseThrow(() -> new RuntimeException("Can't convert " + object.getClass().getSimpleName() + " to " + expectedType.getSimpleName()));
    }

    public <R> boolean hasConverter(Object object, Class<R> expectedType) {
        if (object == null) {
            return false;
        }
        if (ClassUtils.isAssignable(object.getClass(), expectedType)) {
            return true;
        }
        Class<?> givenType = object.getClass();
        return implicitConversionUnits.stream()
                .anyMatch(conversionUnit -> conversionUnit.isApplicable(givenType, expectedType));
    }

    public Stream<Object> getAllPossibleConversions(Object object) {
        Stream<Object> identity = Stream.of(object);
        Stream<Object> convertedValues = implicitConversionUnits.stream()
                .filter(unit -> unit.getFrom() == object.getClass())
                .map(unit -> succeedConversion(unit, object))
                .filter(Optional::isPresent)
                .map(Optional::get);
        return Stream.concat(identity, convertedValues);
    }

    private Optional<Object> succeedConversion(ImplicitConversionUnit<?, ?> unit, Object objectToConvert) {
        try {
            return Optional.ofNullable(unit.convert(objectToConvert));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static Set<ImplicitConversionUnit<?, ?>> numberToBigDecimalConversion() {
        Set<ImplicitConversionUnit<?, ?>> implicitConversionUnits = new HashSet<>();
        implicitConversionUnits.add(new ImplicitConversionUnit<>(Long.class, BigDecimal.class, BigDecimal::valueOf));
        implicitConversionUnits.add(new ImplicitConversionUnit<>(BigDecimal.class, Long.class, BigDecimal::longValueExact));
        implicitConversionUnits.add(new ImplicitConversionUnit<>(Integer.class, BigDecimal.class, BigDecimal::valueOf));
        implicitConversionUnits.add(new ImplicitConversionUnit<>(BigDecimal.class, Integer.class, BigDecimal::intValueExact));
        implicitConversionUnits.add(new ImplicitConversionUnit<>(Short.class, BigDecimal.class, BigDecimal::valueOf));
        implicitConversionUnits.add(new ImplicitConversionUnit<>(BigDecimal.class, Short.class, BigDecimal::shortValueExact));
        implicitConversionUnits.add(new ImplicitConversionUnit<>(Byte.class, BigDecimal.class, BigDecimal::valueOf));
        implicitConversionUnits.add(new ImplicitConversionUnit<>(BigDecimal.class, Byte.class, BigDecimal::byteValueExact));
        implicitConversionUnits.add(new ImplicitConversionUnit<>(Double.class, BigDecimal.class, BigDecimal::valueOf));
        implicitConversionUnits.add(new ImplicitConversionUnit<>(BigDecimal.class, Double.class, BigDecimal::doubleValue));
        implicitConversionUnits.add(new ImplicitConversionUnit<>(Float.class, BigDecimal.class, BigDecimal::valueOf));
        implicitConversionUnits.add(new ImplicitConversionUnit<>(BigDecimal.class, Float.class, BigDecimal::floatValue));
        return implicitConversionUnits;
    }
}
