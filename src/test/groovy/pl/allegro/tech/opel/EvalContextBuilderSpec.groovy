package pl.allegro.tech.opel

import spock.lang.Specification

import static pl.allegro.tech.opel.EvalContextBuilder.create
import static pl.allegro.tech.opel.TestUtil.constFunctionReturning

class EvalContextBuilderSpec extends Specification {
    def "should create merged context using elements from secondary context"() {
        given:
        def primary = create()
                .withCompletedValue('vv', 'value1')
                .withCompletedValue('ff', constFunctionReturning('from fun 1'))
                .build()
        def secondary = create()
                .withCompletedValue('v', 'value2')
                .withCompletedValue('f', constFunctionReturning('from fun 2'))
                .build()

        when:
        def context = EvalContextBuilder.mergeContexts(primary, secondary)

        then:
        context.getValue('v').get().get() == 'value2'
        context.getValue('f').get().get().apply([]).get() == 'from fun 2'
    }

    def "should create merged context using elements from primary context"() {
        given:
        def primary = create()
                        .withCompletedValue('v', 'value1')
                        .withCompletedValue('f', constFunctionReturning('from fun 1'))
                        .build()
        def secondary = create()
                        .withCompletedValue('v', 'value2')
                        .withCompletedValue('f', constFunctionReturning('from fun 2'))
                        .build()

        when:
        def context = EvalContextBuilder.mergeContexts(primary, secondary)

        then:
        context.getValue('v').get().get() == 'value1'
        context.getValue('f').get().get().apply([]).get() == 'from fun 1'
    }

    def "adding new values should not change built context"() {
        given:
        def builder = create()
            .withCompletedValue('a', 'a')
        def context1 = builder.build()

        when:
        builder.withCompletedValue('b', 'b')
        def context2 = builder.build()

        then:
        context1.getValue('a').isPresent()
        !context1.getValue('b').isPresent()
        context2.getValue('a').isPresent()
        context2.getValue('b').isPresent()
    }
}
