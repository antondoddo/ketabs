package com.ketabs.model.valueobject

import arrow.core.computations.ResultEffect.bind
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

internal class PasswordTest {

    @TestFactory
    fun `constructor must prevent invalid value`() = listOf(
        "short" to Password.InvalidPassword.TooShort(),
        "a".repeat(15) to Password.InvalidPassword.TooSimple(),
    ).map { (input, expected) ->
        DynamicTest.dynamicTest("when I try creating a password as `$input` then I expect an error `$expected`") {
            Password.of(input).shouldBeLeft(expected)
        }
    }

    @TestFactory
    fun `constructor must not return the same string`() = listOf(
        "a!1)saoYidn*/#(0nd*)",
        "SecretPassword2&",
    ).map {
        DynamicTest.dynamicTest("when I create a password as `$it` then it must be equal to itself as string") {
            assertNotSame(it, Password.of(it).shouldBeRight().toString())
        }
    }

    @TestFactory
    fun `password are matched when are equals, false otherwise`() = listOf(
        listOf(
            "a!1)saoYidn*/#(0nd*)",
            "a!1)saoYidn*/#(0nd*)",
        ) to true,
        listOf(
            "SecretPassword2&",
            "SecretPassword2&"
        ) to true,
        listOf(
            "a!1)saoYidn*/#(0nd*)",
            "SecretPassword2&",
        ) to false,
    ).map {
        DynamicTest.dynamicTest("when the first password is `${it.first[0]}` and the second one is `${it.first[1]}` then I expect isEqual to return `${it.second}`") {
            assertSame(it.second, Password.of(it.first[0]).bind().matches(it.first[1]))
        }
    }
}
