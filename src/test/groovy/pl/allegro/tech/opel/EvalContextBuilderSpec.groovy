package pl.allegro.tech.opel

import spock.lang.Specification

import static pl.allegro.tech.opel.EvalContextBuilder.create
import static pl.allegro.tech.opel.TestUtil.constFunctionReturning

class EvalContextBuilderSpec extends Specification {
    def "should create merged context using elements from secondary context"() {
        given:
        def primary = create()
                .withCompletedVariable('vv', 'value1')
                .withFunction('ff', constFunctionReturning('from fun 1'))
                .build()
        def secondary = create()
                .withCompletedVariable('v', 'value2')
                .withFunction('f', constFunctionReturning('from fun 2'))
                .build()

        when:
        def context = EvalContextBuilder.mergeContexts(primary, secondary)

        then:
        context.getVariable('v').get().get() == 'value2'
        context.getFunction('f').get().apply([]).get() == 'from fun 2'
    }

    def "should create merged context using elements from primary context"() {
        given:
        def primary = create()
                        .withCompletedVariable('v', 'value1')
                        .withFunction('f', constFunctionReturning('from fun 1'))
                        .build()
        def secondary = create()
                        .withCompletedVariable('v', 'value2')
                        .withFunction('f', constFunctionReturning('from fun 2'))
                        .build()

        when:
        def context = EvalContextBuilder.mergeContexts(primary, secondary)

        then:
        context.getVariable('v').get().get() == 'value1'
        context.getFunction('f').get().apply([]).get() == 'from fun 1'
    }
}
