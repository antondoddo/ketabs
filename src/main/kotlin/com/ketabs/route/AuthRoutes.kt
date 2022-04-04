package com.ketabs.route

import arrow.core.Either
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ketabs.model.User
import com.ketabs.route.request.LoginAuthRequest
import com.ketabs.route.request.RegisterAuthRequest
import com.ketabs.service.LoginAuth
import com.ketabs.service.LoginAuthError
import com.ketabs.service.RegisterAuth
import com.ketabs.service.RegisterAuthError
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import java.util.Date
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

fun Route.auth(
    loginAuth: LoginAuth,
    registerAuth: RegisterAuth,
    secret: String,
    issuer: String,
    audience: String,
) {
    post("/auth/login") {
        let {
            when (val request = Either.catch { call.receive<LoginAuthRequest>() }) {
                is Either.Right -> request.value
                is Either.Left -> when (request.value) {
                    is SerializationException -> return@post call.response.status(HttpStatusCode.BadRequest)
                    else -> return@post call.response.status(HttpStatusCode.InternalServerError)
                }
            }
        }.let {
            when (val parsed = it.parse()) {
                is Either.Right -> parsed.value
                is Either.Left -> return@post call.respond(HttpStatusCode.UnprocessableEntity, parsed.value)
            }
        }.let {
            when (val result = loginAuth(it)) {
                is Either.Right -> return@post call.respond(
                    HttpStatusCode.OK,
                    generateJWT(result.value, secret, issuer, audience)
                )
                is Either.Left -> result.value
            }
        }.let {
            when (it) {
                is LoginAuthError.UserNotFound -> return@post call.respond(HttpStatusCode.Unauthorized)
                is LoginAuthError.ReadError -> return@post call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }

    post("/auth/register") {
        let {
            when (val request = Either.catch { call.receive<RegisterAuthRequest>() }) {
                is Either.Right -> request.value
                is Either.Left -> when (request.value) {
                    is SerializationException -> return@post call.response.status(HttpStatusCode.BadRequest)
                    else -> return@post call.response.status(HttpStatusCode.InternalServerError)
                }
            }
        }.let {
            when (val parsed = it.parse()) {
                is Either.Right -> parsed.value
                is Either.Left -> return@post call.respond(HttpStatusCode.UnprocessableEntity, parsed.value)
            }
        }.let {
            when (val result = registerAuth(it)) {
                is Either.Right -> return@post call.respond(HttpStatusCode.Created, result.value.toResponse())
                is Either.Left -> result.value
            }
        }.let {
            when (it) {
                is RegisterAuthError.WriteError -> return@post call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}

private fun User.toResponse() = mapOf(
    "id" to this.id.toString(),
    "full_name" to this.fullName.toString(),
    "email" to this.email.toString(),
)

private fun generateJWT(
    user: User,
    secret: String,
    issuer: String,
    audience: String,
) = mapOf(
    "token" to JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("id", user.id.value)
        .withClaim("email", user.email.value)
        .withClaim("full_name", user.fullName.value)
        .withExpiresAt(Date(System.currentTimeMillis() + 60000))
        .sign(Algorithm.HMAC256(secret))
)
