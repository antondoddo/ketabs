package com.ketabs.model.valueobject

import arrow.core.computations.ResultEffect.bind
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertSame

internal class FullNameTest {

    @TestFactory
    fun `constructor must prevent invalid value`() = listOf(
        "" to FullName.InvalidFullName.TooShort(),
        "Mark" to FullName.InvalidFullName.TooShort(),
    ).map { (input, expected) ->
        DynamicTest.dynamicTest("when I try creating a full name as `$input` then I expect an error `$expected`") {
            FullName.of(input).shouldBeLeft(expected)
        }
    }

    @TestFactory
    fun `constructor must return the same string`() = listOf(
        "John Williams",
        "Mr. Luke Skywalker",
    ).map {
        DynamicTest.dynamicTest("when I create a full name as `$it` then it must be equal to itself as string") {
            assertSame(it, FullName.of(it).shouldBeRight().toString())
        }
    }

    @TestFactory
    fun `isEqual must return true when to full names are identical, false otherwise`() = listOf(
        listOf(
            FullName.of("Mr. Like Skywalker"),
            FullName.of("Mr. Like Skywalker")
        ) to true,
        listOf(
            FullName.of("John Williams"),
            FullName.of("John Williams")
        ) to true,
        listOf(
            FullName.of("Mr. Like Skywalker"),
            FullName.of("John Williams")
        ) to false,
    ).map {
        DynamicTest.dynamicTest("when the first full name is `${it.first[0]}` and the second one is `${it.first[1]}` then I expect isEqual to return `${it.second}`") {
            assertSame(it.second, it.first[0].bind().isEqual(it.first[1].bind()))
        }
    }
}
