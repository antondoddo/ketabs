package com.ketabs.model.valueobject

import arrow.core.computations.ResultEffect.bind
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertSame

internal class NameTest {

    @TestFactory
    fun `constructor must prevent invalid value`() = listOf(
        "" to Name.InvalidName.TooShort(),
        "Company website".repeat(100) to Name.InvalidName.TooLong(),
    ).map { (input, expected) ->
        DynamicTest.dynamicTest("when I try creating a name as `$input` then I expect an error `$expected`") {
            Name.of(input).shouldBeLeft(expected)
        }
    }

    @TestFactory
    fun `constructor must return the same string`() = listOf(
        "Performance reviews",
        "Google Drive",
    ).map {
        DynamicTest.dynamicTest("when I create a name as `$it` then it must be equal to itself as string") {
            assertSame(it, Name.of(it).shouldBeRight().toString())
        }
    }

    @TestFactory
    fun `isEqual must return true when to names are identical, false otherwise`() = listOf(
        listOf(Name.of("Performance reviews"), Name.of("Performance reviews")) to true,
        listOf(Name.of("Google Drive"), Name.of("Google Drive")) to true,
        listOf(Name.of("Notion documentation"), Name.of("Notion")) to false,
    ).map {
        DynamicTest.dynamicTest("when the first name is `${it.first[0]}` and the second one is `${it.first[1]}` then I expect isEqual to return `${it.second}`") {
            assertSame(it.second, it.first[0].bind().isEqual(it.first[1].bind()))
        }
    }
}
