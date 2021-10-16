[![Build](https://github.com/allegro/opel/actions/workflows/ci.yml/badge.svg)](https://github.com/allegro/opel/actions/workflows/ci.yml)
[![Codecov](https://img.shields.io/codecov/c/github/allegro/opel.svg?style=flat)](https://codecov.io/gh/allegro/opel)
[![GitHub Release Date](https://img.shields.io/github/release-date/allegro/opel.svg?style=flat)](https://github.com/allegro/opel/releases)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)

# opel - asynchronous expression language

opel was designed to let you write simple, short asynchronous expressions. It uses 
[Parboiled](https://github.com/sirthias/parboiled) as a language grammar engine and common Java 8 
[CompletableFuture](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html).

For example `temperature()` function asks REST service for temperature in Fahrenheit for given capital city
and we want to convert it to Celsius. We can write a simple expression to achievie it.

```
(temperature('Warsaw') - 32) * 5 / 9
```

this expression will be transformed to equivalent code:

```
temperature('Warsaw')
	.thenCombine(CompletableFuture.completedFuture(32), (l, r) -> l - r)
	.thenCombine(CompletableFuture.completedFuture(5), (l, r) -> l * r)
	.thenCombine(CompletableFuture.completedFuture(9), (l, r) -> l / r)
```

## Contents
* [Our business case](#our-business-case)
* [Why have we created another language?](#why-have-we-created-another-language)
* [What can opel do for you?](#what-can-opel-do-for-you)
* [What opel can't do?](#what-opel-cant-do)
* [Using with Gradle](#using-with-gradle)

## <a name="our-business-case"></a>Our business case

In [OpBox](http://allegro.tech/2016/03/Managing-Frontend-in-the-microservices-architecture.html)
which is our solution to build frontend in microservices world we have to prepare site title depending on data returned 
from data source (REST service). For show product page it may look like:

```
restService('showProduct') + ' - Allegro.pl - Więcej niż aukcje.'
```

## Why have we created another language?

Allegro is a big platform with huge traffic. We encounter many performance issues so we decided 
to design everything asynchronously. Whenever it's possible we use CompletableFutures. It's a bit more complicated that way
but opel abstraction layer enables our users (non developers) to write simple expressions in easy way.

After checking some expression languages like: 
[SpEL](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/expressions.html) or 
[JEXL](http://commons.apache.org/proper/commons-jexl/) we didn't find any libraries supporting required asynchronous behavior.

## What opel can't do?

opel aims at very simple expressions. Certainly it won't be enough to make complicated scripts - but we want opel to stay that way.

## What can opel do for you?

opel supports:

- primary math and string operations (i.e. `2+2*2`, `'Hello' + 'world !'`)
- relational and equality operators (i.e. `2 == 3`, `2 > 1 != false`)
- logic operators (i.e. `true && false`, `false || true`)
- simple map element access (i.e. `map.field` or `map['field']`)
- simple list element access (i.e. `list[index]`)
- object method calls (i.e. `'Hello, World!'.length()`)
- if expressions (i.e. `if (2 > 3) 'a' else 'b'`)
- defining local constant values (i.e. `val x = 2+2*2; x * x`)
- defining maps (i.e. `val x = {'a': 'b'}; x.a`)
- defining lists (i.e. `val x = ['a', 'b']; x[0]`)
- defining the functions and lambda expression (i.e. `val x = a -> a * a; val y = b -> b + b; x(y(r))`)
- registrable constant values (i.e. `'Hello, ' + WORLD_VALUE`)
- registrable functions (i.e. `myFunction('Hello, World!')`)
- registrable implicit conversions (i.e. `2 + '2'` or `'Hello, World!'.myMethod()`)

More can be found in [documentation](https://github.com/allegro/opel/wiki).

## Using with Gradle

Basically, all you have to do is to add a compile dependency:

```
dependencies {
    compile 'pl.allegro.tech:opel:1.1.8'
}
```

## Java usage examples

### Evaluate simple expression

Create an instance of `OpelEngine` and evaluate the expression:

```
OpelEngine engine = OpelEngineBuilder.create()
        .build();

engine.eval("2 + 3")
        .whenComplete((result, error) -> System.out.println(result));
```

### Evaluate expression with global variable

Create an instance of `OpelEngine` with global a variable and evaluate the expression:

```
OpelEngine engine = OpelEngineBuilder.create()
        .withCompletedValue("PI", 3.14)
        .build();

engine.eval("PI * 2")
        .whenComplete((result, error) -> System.out.println(result));
```

Notice that in opel, all variables are final.

### Evaluate expression with context variable

The engine is a heavy object and should be reused to evaluate different expressions.
To achieve this, variables can be provided in the context:

```
OpelEngine engine = OpelEngineBuilder.create()
        .withCompletedValue("PI", 3.14)
        .build();

String expression = "PI * r * r";

EvalContext context = EvalContextBuilder.create()
        .withCompletedValue("r", 3)
        .build();

engine.eval(expression, context)
        .whenComplete((result, error) -> System.out.println(result));
```

In the engine, you can configure general language for the application. 
In context, you can provide, for example, request context like authorized username.

### Evaluate expression with engine/context function

Functions in opel are implemented by `OpelAsyncFunction` interface and can be added as regular variable:

```
OpelAsyncFunction<Object> function = new OpelAsyncFunction<Object>() {
            @Override
            CompletableFuture<Object> apply(List<CompletableFuture<?>> args) {
                Object result = // a call to an external service, to a database or other logic
                return result;
            }
        };

OpelEngine engine = OpelEngineBuilder.create()
        .withCompletedValue("myFun", function)
        .build();

String expression = "myFun(a) * myFun(b)";

EvalContext context = EvalContextBuilder.create()
        .withCompletedValue("a", "john")
        .withCompletedValue("b", "jenny")
        .build();

engine.eval(expression, context)
        .whenComplete((result, error) -> System.out.println(result));
```

In the same way, we add function to `OpelEngine` by `withCompletedValue` method, it can be added to `EvalContext`.
