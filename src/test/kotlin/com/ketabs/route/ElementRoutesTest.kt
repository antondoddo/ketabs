package com.ketabs.route

import com.ketabs.ObjectMother
import com.ketabs.testApp
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ElementRoutesTest {

    @TestFactory
    fun `user must not create elements when invalid data are sent`(): List<DynamicTest> {
        val tab = ObjectMother.randomTabElement()
        val collection = ObjectMother.randomCollectionElement()

        return listOf(
            Triple(
                "tab name is too short",
                mapOf(
                    "name" to "a",
                    "description" to tab.description.value,
                    "link" to tab.link.value
                )
            ) { data: Map<String, Map<String, String>> ->
                assertEquals("Name must be at least 2 chars", data["errors"]?.get("name"))
            },
            Triple(
                "collection name is too short",
                mapOf(
                    "name" to "b",
                    "description" to collection.description.value,
                )
            ) { data: Map<String, Map<String, String>> ->
                assertEquals("Name must be at least 2 chars", data["errors"]?.get("name"))
            },
            Triple(
                "tab name and description are too long",
                mapOf(
                    "name" to "a".repeat(200),
                    "description" to "a1".repeat(250),
                    "link" to tab.link.value
                )
            ) { data: Map<String, Map<String, String>> ->
                assertEquals("Name must be at max 100 chars", data["errors"]?.get("name"))
                assertEquals("Description must not be longer than 200 chars", data["errors"]?.get("description"))
            },
            Triple(
                "collection name and description are too long",
                mapOf(
                    "name" to "b".repeat(200),
                    "description" to "b1".repeat(250),
                )
            ) { data: Map<String, Map<String, String>> ->
                assertEquals("Name must be at max 100 chars", data["errors"]?.get("name"))
                assertEquals("Description must not be longer than 200 chars", data["errors"]?.get("description"))
            },
        ).map { (failure, data, assertion) ->
            DynamicTest.dynamicTest("when I try creating an failure with invalid data then I must receive `$failure`") {
                withTestApplication(testApp()) {
                    with(
                        handleRequest(HttpMethod.Post, "/elements") {
                            setBody(Json.encodeToString(data))
                            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                            addHeader(HttpHeaders.Authorization, "Bearer ${ObjectMother.randomJWT()}")
                        }
                    ) {
                        assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                        assertion(Json.decodeFromString(response.content ?: "{}"))
                    }
                }
            }
        }
    }

    @TestFactory
    fun `user must not create elements when unauthorized`(): List<DynamicTest> {
        val tab = ObjectMother.randomTabElement()
        val collection = ObjectMother.randomCollectionElement()

        return listOf(
            Pair(
                "tab",
                mapOf(
                    "name" to tab.name.value,
                    "description" to tab.description.value,
                    "link" to tab.link.value
                )
            ),
            Pair(
                "collection",
                mapOf(
                    "name" to collection.name.value,
                    "description" to collection.description.value,
                )
            ),
        ).map { (element, data) ->
            DynamicTest.dynamicTest("when I try creating a `$element` element  when I am not authorized then I must be rejected") {
                withTestApplication(testApp()) {
                    with(
                        handleRequest(HttpMethod.Post, "/elements") {
                            setBody(Json.encodeToString(data))
                            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                            addHeader(HttpHeaders.Authorization, "Bearer InvalidToken")
                        }
                    ) {
                        assertNull(response.content)
                        assertEquals(HttpStatusCode.Unauthorized, response.status())
                    }
                }
            }
        }
    }
}
