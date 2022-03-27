package com.ketabs.model

import com.ketabs.ObjectMother
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class ElementTest {

    @Test
    fun `create collection must return the expected values`() {
        val name = ObjectMother.randomName()
        val description = ObjectMother.randomDescription()

        val collection = Element.Collection.create(name, description)

        assertEquals(name, collection.name)
        assertEquals(description, collection.description)
    }

    @Test
    fun `create collection with parent must return the expected values`() {
        val name = ObjectMother.randomName()
        val description = ObjectMother.randomDescription()
        val parent = ObjectMother.randomCollectionElement()

        val collection = Element.Collection.createWithParent(name, description, parent)

        assertEquals(name, collection.name)
        assertEquals(description, collection.description)
        assertEquals(parent, collection.parent)
    }

    @Test
    fun `create tab must return the expected values`() {
        val name = ObjectMother.randomName()
        val description = ObjectMother.randomDescription()
        val link = ObjectMother.randomLink()

        val collection = Element.Tab.create(name, description, link)

        assertEquals(name, collection.name)
        assertEquals(description, collection.description)
        assertEquals(link, collection.link)
    }

    @Test
    fun `create tab with parent must return the expected values`() {
        val name = ObjectMother.randomName()
        val description = ObjectMother.randomDescription()
        val link = ObjectMother.randomLink()
        val parent = ObjectMother.randomCollectionElement()

        val tab = Element.Tab.createWithParent(name, description, link, parent)
        assertEquals(name, tab.name)
        assertEquals(description, tab.description)
        assertEquals(link, tab.link)
        assertEquals(parent, tab.parent)
    }

    @TestFactory
    fun `element has parent`() = listOf(
        ObjectMother.randomCollectionElementWithParent() to true,
        ObjectMother.randomTabElementWithParent() to true,
        ObjectMother.randomCollectionElement() to false,
        ObjectMother.randomTabElement() to false,
    ).map { (input, expected) ->
        DynamicTest.dynamicTest("given an element when I check if it has a parent then I expect it to be `$expected`") {
            assertEquals(expected, input.hasParent())
        }
    }

    @Test
    fun `element must have a new name`() {
        val name = ObjectMother.randomName()
        val element = ObjectMother.randomCollectionElement()

        element.withName(name).let {
            assertNotEquals(name, element.name)
            assertEquals(name, it.name)
        }
    }

    @Test
    fun `element must have a new description`() {
        val description = ObjectMother.randomDescription()
        val element = ObjectMother.randomCollectionElement()

        element.withDescription(description).let {
            assertNotEquals(description, element.description)
            assertEquals(description, it.description)
        }
    }

    @Test
    fun `tab element must have a new link`() {
        val link = ObjectMother.randomLink()
        val element = ObjectMother.randomTabElement()

        element.withLink(link).let {
            assertNotEquals(link, element.link)
            assertEquals(link, it.link)
        }
    }
}
