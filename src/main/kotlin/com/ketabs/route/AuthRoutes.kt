package com.ketabs.route

import arrow.core.Either
import arrow.core.computations.ResultEffect.bind
import arrow.core.computations.either
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ketabs.model.User
import com.ketabs.route.request.LoginAuthRequest
import com.ketabs.route.request.RegisterAuthRequest
import com.ketabs.route.response.Response
import com.ketabs.route.response.withResponseHandler
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
import kotlinx.serialization.SerializationException
import java.util.Date

fun Route.auth(
    loginAuth: LoginAuth,
    registerAuth: RegisterAuth,
    secret: String,
    issuer: String,
    audience: String,
) {
    post("/auth/login") {
        call.withResponseHandler {
            either {
                val req = Either.catch { call.receive<LoginAuthRequest>() }.tapLeft {
                    when (it) {
                        is SerializationException -> Response.Failure { a -> a.respond(HttpStatusCode.BadRequest) }
                        else -> Response.Failure { a -> a.respond(HttpStatusCode.InternalServerError) }
                    }
                }.bind()

                val data = req.parse().mapLeft {
                    Response.Failure { a -> a.respond(HttpStatusCode.UnprocessableEntity, it) }
                }.bind()

                val result = loginAuth(data).mapLeft {
                    when (it) {
                        is LoginAuthError.UserNotFound -> Response.Failure { a -> a.respond(HttpStatusCode.Unauthorized) }
                        is LoginAuthError.ReadError -> Response.Failure { a -> a.respond(HttpStatusCode.InternalServerError) }
                    }
                }.bind()

                Response.Success {
                    it.respond(
                        HttpStatusCode.OK,
                        generateJWT(result, secret, issuer, audience)
                    )
                }
            }
        }
    }

    post("/auth/register") {
        call.withResponseHandler {
            either {
                val req = Either.catch { call.receive<RegisterAuthRequest>() }.tapLeft {
                    when (it) {
                        is SerializationException -> Response.Failure { a -> a.respond(HttpStatusCode.BadRequest) }
                        else -> Response.Failure { a -> a.respond(HttpStatusCode.InternalServerError) }
                    }
                }.bind()

                val data = req.parse().mapLeft {
                    Response.Failure { a -> a.respond(HttpStatusCode.UnprocessableEntity, it) }
                }.bind()

                val result = registerAuth(data).mapLeft {
                    when (it) {
                        is RegisterAuthError.WriteError -> Response.Failure { a -> a.respond(HttpStatusCode.Unauthorized) }
                    }
                }.bind()

                Response.Success {
                    it.respond(HttpStatusCode.Created, result.toResponse())
                }
            }
        }
    }
}

private fun User.toResponse() = mapOf(
    "id" to this.id.value,
    "full_name" to this.fullName.value,
    "email" to this.email.value,
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
