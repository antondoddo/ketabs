package com.ketabs.model.valueobject

import arrow.core.computations.ResultEffect.bind
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertSame

internal class EmailTest {

    @TestFactory
    fun `constructor must prevent invalid value`() = listOf(
        "name.domain.com" to Email.InvalidEmail.InvalidFormat(),
        "name @domain.com" to Email.InvalidEmail.InvalidFormat(),
    ).map { (input, expected) ->
        DynamicTest.dynamicTest("when I try creating a email as `$input` then I expect an error `$expected`") {
            Email.of(input).shouldBeLeft(expected)
        }
    }

    @TestFactory
    fun `constructor must return the same string`() = listOf(
        "name@domain.com",
        "name@domain.io",
    ).map {
        DynamicTest.dynamicTest("when I create a email as `$it` then it must be equal to itself as string") {
            assertSame(it, Email.of(it).shouldBeRight().toString())
        }
    }

    @TestFactory
    fun `isEqual must return true when to emails are identical, false otherwise`() = listOf(
        listOf(
            Email.of("name@domain.com"),
            Email.of("name@domain.com")
        ) to true,
        listOf(
            Email.of("name@domain.io"),
            Email.of("name@domain.io")
        ) to true,
        listOf(
            Email.of("name@domain.com"),
            Email.of("name@domain.io")
        ) to false,
    ).map {
        DynamicTest.dynamicTest("when the first email is `${it.first[0]}` and the second one is `${it.first[1]}` then I expect isEqual to return `${it.second}`") {
            assertSame(it.second, it.first[0].bind().isEqual(it.first[1].bind()))
        }
    }
}
