package pl.allegro.tech.opel

import java.util.concurrent.CompletableFuture

class TestUtil {
    static functions() {
        def functionWith2Args = function({ args ->
            args[0] == 'x' && args[1] == 'y' ? 'xAndY' : 'otherThanXAndY'
        })
        def functionWith4Args = function({ args ->
            args.join("")
        })
        def identityFunction = function({ args ->
            args[0]
        })
        return [
                'zero'        : CompletableFuture.completedFuture((OpelAsyncFunction<?>) constFunctionReturning('zero')),
                'one'         : CompletableFuture.completedFuture((OpelAsyncFunction<?>) constFunctionReturning('one')),
                'twoArgsFunc' : CompletableFuture.completedFuture(functionWith2Args),
                'oneTwoThree' : CompletableFuture.completedFuture(constFunctionReturning('one two three')),
                'fourArgsFunc': CompletableFuture.completedFuture(functionWith4Args),
                'identity'    : CompletableFuture.completedFuture(identityFunction)
        ];
    }

    // Allows to define simple function which returns always 'result' ignoring passed arguments
    static def constFunctionReturning(def result) {
        function({ args -> result })
    }

    static function(def body) {
        return { args ->
            FutureUtil.sequence(args).thenApply { completedArgs ->
                body(completedArgs)
            }
        } as OpelAsyncFunction
    }
}
