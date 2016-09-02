package pl.allegro.tech.opel

import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.Collectors

class ImplicitConversionSpec extends Specification {

    def string2intConversion = new ImplicitConversionUnit<String, Integer>(String, Integer, { str -> Integer.parseInt(str) })
    def int2StringConversion = new ImplicitConversionUnit<Integer, String>(Integer, String, { num -> Integer.toString(num) })
    def string2doubleConversion = new ImplicitConversionUnit<String, Double>(String, Double, { str -> Double.parseDouble(str) })
    def double2integerConversion = new ImplicitConversionUnit<Double, Integer>(BigDecimal, Integer, { d -> d.intValueExact() })

    @Unroll
    def "should find registered converter if conversion is needed (#object to #expectedType)"() {
        given:
        def implicitConversion = new ImplicitConversion()

        implicitConversion.register(int2StringConversion)
        implicitConversion.register(string2doubleConversion)

        expect:
        implicitConversion.hasConverter(object, expectedType) == result

        where:
        object | expectedType || result
        123    | String       || true
        123    | Double       || false
        123L   | String       || false
        '123'  | Double       || true
        '123'  | Integer      || false
        '123'  | String       || true
        123    | Integer      || true
        1.1f   | Float        || true
    }

    @Unroll
    def "should convert object to expected type (#object to #expectedType)"() {
        given:
        def implicitConversion = new ImplicitConversion()
        implicitConversion.register(int2StringConversion)
        implicitConversion.register(string2doubleConversion)

        expect:
        implicitConversion.convert(object, expectedType) == result

        where:
        object | expectedType || result
        123    | String       || '123'
        '123'  | Double       || 123d
        '123'  | String       || '123'
        123    | Integer      || 123
        1.1f   | Float        || 1.1f
    }

    @Unroll
    def "should convert #object to all possible types"() {
        given:
        def implicitConversion = new ImplicitConversion()
        implicitConversion.register(int2StringConversion)
        implicitConversion.register(string2doubleConversion)
        implicitConversion.register(string2intConversion)

        expect:
        implicitConversion.getAllPossibleConversions(object).collect(Collectors.toSet()) == result as Set

        where:
        object || result
        123    || [123, '123']
        '124'  || ['124', 124d, 124]
        125f   || [125f]
    }

    @Unroll
    def "should skip failed conversion when convert to all possible types"() {
        def implicitConversion = new ImplicitConversion()
        implicitConversion.register(string2intConversion)
        implicitConversion.register(double2integerConversion)

        expect:
        implicitConversion.getAllPossibleConversions(object).collect(Collectors.toSet()) == result as Set

        where:
        object        || result
        'notIntValue' || ['notIntValue']
        '111.22'      || ['111.22']
        111.22        || [111.22]

    }

    def "should prevent to override registered conversion when new one has the same from and to types"() {
        given:
        def implicitConversion = new ImplicitConversion()
        implicitConversion.register(int2StringConversion)

        when:
        implicitConversion.register(new ImplicitConversionUnit<Integer, String>(Integer, String, { num -> 'i:' + Integer.toString(num) }))

        then:
        implicitConversion.convert(123, String) == '123'
        implicitConversion.implicitConversionUnits.size() == 1
    }
}
