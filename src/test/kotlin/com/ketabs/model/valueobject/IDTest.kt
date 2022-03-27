package com.ketabs.model.valueobject

import arrow.core.computations.ResultEffect.bind
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

internal class IDTest {

    @TestFactory
    fun `constructor must prevent invalid value`() = listOf(
        "" to ID.InvalidID.InvalidFormat(),
        "36295794-4a9a-4263-ae0423eab1de656" to ID.InvalidID.InvalidFormat(),
    ).map { (input, expected) ->
        DynamicTest.dynamicTest("when I try creating a id as `$input` then I expect an error `$expected`") {
            ID.of(input).shouldBeLeft(expected)
        }
    }

    @TestFactory
    fun `constructor must return the same string`() = listOf(
        "9ce43ae6-551e-42b0-8e9c-0a3eaef8b2b5",
        "21b6604c-f42d-4c3f-a2d4-c4f9f9c387f4",
        "36295794-4a9a-4263-ae04-23eab1de6564",
    ).map {
        DynamicTest.dynamicTest("when I create a id as `$it` then it must be equal to itself as string") {
            assertSame(it, ID.of(it).shouldBeRight().toString())
        }
    }

    @TestFactory
    fun `isEqual must return true when to ids are identical, false otherwise`() = listOf(
        listOf(
            ID.of("9ce43ae6-551e-42b0-8e9c-0a3eaef8b2b5"),
            ID.of("9ce43ae6-551e-42b0-8e9c-0a3eaef8b2b5")
        ) to true,
        listOf(
            ID.of("36295794-4a9a-4263-ae04-23eab1de6564"),
            ID.of("36295794-4a9a-4263-ae04-23eab1de6564")
        ) to true,
        listOf(
            ID.of("36295794-4a9a-4263-ae04-23eab1de6564"),
            ID.of("36295794-4a9a-4263-ae04-23eab1de6563")
        ) to false,
    ).map {
        DynamicTest.dynamicTest("when the first id is `${it.first[0]}` and the second one is `${it.first[1]}` then I expect isEqual to return `${it.second}`") {
            assertSame(it.second, it.first[0].bind().isEqual(it.first[1].bind()))
        }
    }

    @TestFactory
    fun `generate must not return two different IDs`() {
        assertNotEquals(ID.random(), ID.random())
    }
}
