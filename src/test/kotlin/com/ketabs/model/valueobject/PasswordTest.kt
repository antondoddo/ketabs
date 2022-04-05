package com.ketabs.model.valueobject

import arrow.core.computations.ResultEffect.bind
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertNotSame
import kotlin.test.assertSame

internal class PasswordTest {

    @TestFactory
    fun `constructor must prevent invalid value`() = listOf(
        "short" to Password.InvalidPassword.TooShort,
        "a".repeat(15) to Password.InvalidPassword.TooSimple,
    ).map { (input, expected) ->
        DynamicTest.dynamicTest("when I try creating a password as `$input` then I expect an error `$expected`") {
            Password.PlainPassword.of(input).shouldBeLeft(expected)
            Password.EncryptedPassword.of(input).shouldBeLeft(expected)
        }
    }

    @TestFactory
    fun `constructor must not return the same string`() = listOf(
        "a!1)saoYidn*/#(0nd*)",
        "SecretPassword2&",
    ).map {
        DynamicTest.dynamicTest("when I create a password as `$it` then it must be equal to itself as string") {
            assertNotSame(it, Password.EncryptedPassword.of(it).shouldBeRight().toString())
        }
    }

    @TestFactory
    fun `constructor must return the same string`() = listOf(
        "a!1)saoYidn*/#(0nd*)",
        "SecretPassword2&",
    ).map {
        DynamicTest.dynamicTest("when I create a password as `$it` then it must be equal to itself as string") {
            assertSame(it, Password.PlainPassword.of(it).shouldBeRight().value)
        }
    }

    @TestFactory
    fun `password are matched when are equals, false otherwise`() = listOf(
        Pair(
            "a!1)saoYidn*/#(0nd*)",
            Password.PlainPassword.of("a!1)saoYidn*/#(0nd*)").bind(),
        ) to true,
        Pair(
            "SecretPassword2&",
            Password.PlainPassword.of("SecretPassword2&").bind(),
        ) to true,
        Pair(
            "a!1)saoYidn*/#(0nd*)",
            Password.PlainPassword.of("SecretPassword2&").bind(),
        ) to false,
    ).map {
        DynamicTest.dynamicTest("when the first password is `${it.first.first}` and the second one is `${it.first.second}` then I expect isEqual to return `${it.second}`") {
            assertSame(it.second, Password.EncryptedPassword.of(it.first.first).bind().matches(it.first.second))
        }
    }
}
