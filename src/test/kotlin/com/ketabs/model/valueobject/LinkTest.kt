package com.ketabs.model.valueobject

import arrow.core.computations.ResultEffect.bind
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertSame

internal class LinkTest {

    @TestFactory
    fun `constructor must prevent invalid value`() = listOf(
        "http:" to Link.InvalidLink.InvalidFormat,
        "ttps://stackoverflow.com/ques" to Link.InvalidLink.InvalidFormat,
    ).map { (input, expected) ->
        DynamicTest.dynamicTest("when I try creating a link as `$input` then I expect an error `$expected`") {
            Link.of(input).shouldBeLeft(expected)
        }
    }

    @TestFactory
    fun `constructor must return the same url`() = listOf(
        "https://www.google.com",
        "https://stackoverflow.com/questions/6038061/regular-expression-to-find-urls-within-a-string",
        "https://ketabs.io/elements/1",
    ).map {
        DynamicTest.dynamicTest("when I create a link as `$it` then it must be equal to itself as string") {
            assertSame(it, Link.of(it).shouldBeRight().value)
        }
    }

    @TestFactory
    fun `isEqual must return true when to links are identical, false otherwise`() = listOf(
        listOf(Link.of("https://www.google.com"), Link.of("https://www.google.com")) to true,
        listOf(Link.of("https://ketabs.io/elements/1"), Link.of("https://ketabs.io/elements/1")) to true,
        listOf(Link.of("https://ketabs.io/elements/2"), Link.of("https://ketabs.io/elements/1")) to false,
    ).map {
        DynamicTest.dynamicTest("when the first link is `${it.first[0]}` and the second one is `${it.first[1]}` then I expect isEqual to return `${it.second}`") {
            assertSame(it.second, it.first[0].bind() == it.first[1].bind())
        }
    }
}
