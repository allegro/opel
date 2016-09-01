package pl.allegro.opbox.opel

import spock.lang.Specification

class ImplicitConversionUnitSpec extends Specification {

    def "should convert using convert function"() {
        given:
        def int2str = new ImplicitConversionUnit(Integer, String, { i -> "i:" + Integer.toString(i) })

        expect:
        int2str.convert(123) == 'i:123'
    }

    def "should throw an exception when required conversion is not found"() {
        given:
        def int2str = new ImplicitConversionUnit(Integer, String, { i -> "i:" + Integer.toString(i) })

        when:
        int2str.convert(123L)

        then:
        thrown RuntimeException
    }

}
