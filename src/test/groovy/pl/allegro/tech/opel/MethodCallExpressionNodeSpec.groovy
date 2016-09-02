package pl.allegro.tech.opel

import spock.lang.Specification

import java.lang.reflect.Method
import java.util.concurrent.ExecutionException

class MethodCallExpressionNodeSpec extends Specification {
    def "should call a zero arguments method"() {
        given:
        def methodCallNode = new MethodCallExpressionNode(valueNode(['a']), 'size', Optional.empty(), new ImplicitConversion(), MethodExecutionFilters.ALLOW_ALL)

        when:
        def result = methodCallNode.getValue().get()

        then:
        result == 1
    }

    def "should call a method with one argument"() {
        given:
        def arguments = Optional.of(new ArgumentsListExpressionNode(valueNode(0)))
        def methodCallNode = new MethodCallExpressionNode(valueNode(['a']), 'get', arguments, new ImplicitConversion(), MethodExecutionFilters.ALLOW_ALL)

        when:
        def result = methodCallNode.getValue().get()

        then:
        result == 'a'
    }

    def "should call a method with two arguments"() {
        given:
        def arguments = Optional.of(new ArgumentsListExpressionNode(valueNode('second'), new ArgumentsListExpressionNode(valueNode('first'))))
        def methodCallNode = new MethodCallExpressionNode(valueNode(new Foo()), 'method', arguments, new ImplicitConversion(), MethodExecutionFilters.ALLOW_ALL)

        when:
        def result = methodCallNode.getValue().get()

        then:
        result == 'firstsecond'
    }

    def "should not allow to call any method when using DENY_ALL method execution filter"() {
        given:
        def arguments = Optional.of(new ArgumentsListExpressionNode(valueNode('second'), new ArgumentsListExpressionNode(valueNode('first'))))
        def methodCallNode = new MethodCallExpressionNode(valueNode(new Foo()), 'method', arguments, new ImplicitConversion(), MethodExecutionFilters.DENY_ALL)

        when:
        methodCallNode.getValue().get()

        then:
        thrown ExecutionException
    }

    def "should not call any method not passing custom method execution filter"() {
        given:
        def arguments = Optional.of(new ArgumentsListExpressionNode(valueNode('second'), new ArgumentsListExpressionNode(valueNode('first'))))
        def methodNameFilter = { Object subject, Method method -> return method.getName() != 'method' }
        def methodCallNode = new MethodCallExpressionNode(valueNode(new Foo()), 'method', arguments, new ImplicitConversion(), methodNameFilter)

        when:
        methodCallNode.getValue().get()

        then:
        thrown ExecutionException
    }

    class Foo {
        def method(a, b) {
            a + b
        }
    }

    private static ValueExpressionNode valueNode(Object o) {
        new ValueExpressionNode(o);
    }
}
