package com.ketabs.route

import arrow.core.computations.ResultEffect.bind
import com.auth0.jwt.JWT
import com.ketabs.ObjectMother
import com.ketabs.model.User
import com.ketabs.model.valueobject.ID
import com.ketabs.repository.InMemoryUserRepository
import com.ketabs.testApp
import io.kotest.common.runBlocking
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
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class AuthRoutesTest {

    @Test
    fun `user must register`() {
        val (user, plainPassword) = ObjectMother.randomUserAndPlainPassword()
        val userRepo = InMemoryUserRepository()

        withTestApplication(testApp(userRepo = userRepo)) {
            with(
                handleRequest(HttpMethod.Post, "/auth/register") {
                    setBody(
                        Json.encodeToString(
                            mapOf(
                                "email" to user.email.value,
                                "password" to plainPassword.value,
                                "full_name" to user.fullName.value
                            )
                        )
                    )
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }
            ) {
                val resData = Json.decodeFromString<Map<String, String>>(response.content ?: "{}")
                assertEquals(HttpStatusCode.Created, response.status())
                assertEquals(user.email.value, resData["email"])
                assertEquals(user.fullName.value, resData["full_name"])
                runBlocking {
                    val found = userRepo.getByID(ID.of(resData["id"] ?: "").bind()).bind()
                    assertEquals(user.email.value, found.email.value)
                    assertEquals(user.fullName.value, found.fullName.value)
                    assertTrue(found.password.matches(plainPassword))
                }
            }
        }
    }

    @Test
    fun `user must log in`() {
        val (user, plainPassword) = ObjectMother.randomUserAndPlainPassword()

        val store = ConcurrentHashMap<ID, User>()
        store[user.id] = user
        val userRepo = InMemoryUserRepository.withStore(store)

        withTestApplication(testApp(userRepo = userRepo)) {
            with(
                handleRequest(HttpMethod.Post, "/auth/login") {
                    setBody(
                        Json.encodeToString(
                            mapOf(
                                "email" to user.email.value,
                                "password" to plainPassword.value,
                            )
                        )
                    )
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }
            ) {
                val resData = Json.decodeFromString<Map<String, String>>(response.content ?: "{}")
                assertEquals(HttpStatusCode.OK, response.status())
                assertContains(resData, "token")
                val decodedToken = JWT.decode(resData["token"])
                assertEquals(user.id.value, decodedToken.getClaim("id").asString())
                assertEquals(user.email.value, decodedToken.getClaim("email").asString())
                assertEquals(user.fullName.value, decodedToken.getClaim("full_name").asString())
            }
        }
    }

    @TestFactory
    fun `user must not log in`(): List<DynamicTest> {
        val (registeredUser, registeredUserPlainPassword) = ObjectMother.randomUserAndPlainPassword()
        val (unregisteredUser, unregisteredUserPlainPassword) = ObjectMother.randomUserAndPlainPassword()

        val store = ConcurrentHashMap<ID, User>()
        store[registeredUser.id] = registeredUser
        val userRepo = InMemoryUserRepository.withStore(store)

        return listOf(
            Pair(registeredUser.email.value, registeredUserPlainPassword.value + "hello"),
            Pair(unregisteredUser.email.value, unregisteredUserPlainPassword.value),
        ).map { (email, password) ->
            DynamicTest.dynamicTest("when I try logging in is as `$email` with password `$password` then I must be unauthorized") {
                withTestApplication(testApp(userRepo = userRepo)) {
                    with(
                        handleRequest(HttpMethod.Post, "/auth/login") {
                            setBody(
                                Json.encodeToString(
                                    mapOf(
                                        "email" to email, "password" to password
                                    )
                                )
                            )
                            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
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
