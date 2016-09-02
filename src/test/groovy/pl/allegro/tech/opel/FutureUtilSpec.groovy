package pl.allegro.tech.opel

import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class FutureUtilSpec extends Specification {

    def executor = Executors.newFixedThreadPool(2)

    def 'should convert list of futures to futures list'() {
        given:
        def futureA = CompletableFuture.supplyAsync({ 'a' }, executor)
        def futureB = CompletableFuture.supplyAsync({ 'b' }, executor)
        def futureC = CompletableFuture.completedFuture('c')

        when:
        def futures = FutureUtil.sequence([futureA, futureB, futureC])

        then:
        futures.join() == ['a', 'b', 'c']
    }


}
