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
	.thenCombine(CompletableFuture.completedFuture(329 (l, r) -> l / r)
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

We discourage using *anonymous functions*, *code blocks*, *loops*, etc.

## What can opel do for you?

opel supports:

- primary math and string operations (i.e. `2+2*2`)
- relational and equality operators (i.e. `2 == 3`, `2 > 1 != false`)
- logic operators (i.e. `true && false`, `false || true`)
- simple map element access (i.e. `map.field` or `map['field']`)
- simple list element access (i.e. `list[index]`)
- object method calls (i.e. `'Hello, World!'.length()`)
- if expressions (i.e. `if (2 > 3) 'a' else 'b'`)
- defining local final variables (i.e. `def x = 2+2*2; x * x`)
- registrable implicit conversions (i.e. `2 + '2'` or `'Hello, World!'.myMethod()`)
- registrable functions (i.e. `myFunction('Hello, World!')`)
- registrable constant values (i.e. `'Hello, ' + WORLD_VALUE`)

More can be found in [documentation](https://github.com/allegro/opel/wiki).

## Using with Gradle

Basically, all you have to do is to add a compile dependency:

```
dependencies {
    compile 'pl.allegro.tech:opel:1.0.1'
}
```
