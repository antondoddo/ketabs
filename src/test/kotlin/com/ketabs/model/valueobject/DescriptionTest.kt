package com.ketabs.model.valueobject

import arrow.core.computations.ResultEffect.bind
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertSame

internal class DescriptionTest {

    @TestFactory
    fun `constructor must prevent invalid value`() = listOf(
        "Company website".repeat(100) to Description.InvalidDescription.TooLong,
    ).map { (input, expected) ->
        DynamicTest.dynamicTest("when I try creating a description as `$input` then I expect an error `$expected`") {
            Description.of(input).shouldBeLeft(expected)
        }
    }

    @TestFactory
    fun `constructor must return the same string`() = listOf(
        "",
        "A great article about Kotlin",
        "PHP is dead . long live PHP!",
    ).map {
        DynamicTest.dynamicTest("when I create a description as `$it` then it must be equal to itself as string") {
            assertSame(it, Description.of(it).shouldBeRight().value)
        }
    }

    @TestFactory
    fun `isEqual must return true when to descriptions are identical, false otherwise`() = listOf(
        listOf(
            Description.of("A great article about Kotlin"),
            Description.of("A great article about Kotlin")
        ) to true,
        listOf(
            Description.of("PHP is dead . long live PHP!"),
            Description.of("PHP is dead . long live PHP!")
        ) to true,
        listOf(
            Description.of("PHP is dead . long live PHP!"),
            Description.of("Java is dead . long live Java!")
        ) to false,
    ).map {
        DynamicTest.dynamicTest("when the first description is `${it.first[0]}` and the second one is `${it.first[1]}` then I expect isEqual to return `${it.second}`") {
            assertSame(it.second, it.first[0].bind() == (it.first[1].bind()))
        }
    }
}
