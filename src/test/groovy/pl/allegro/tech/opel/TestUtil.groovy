package pl.allegro.tech.opel

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
                'zero'        : (OpelAsyncFunction<?>) constFunctionReturning('zero'),
                'one'         : (OpelAsyncFunction<?>) constFunctionReturning('one'),
                'twoArgsFunc' : functionWith2Args,
                'oneTwoThree' : constFunctionReturning('one two three'),
                'fourArgsFunc': functionWith4Args,
                'identity'    : identityFunction
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
