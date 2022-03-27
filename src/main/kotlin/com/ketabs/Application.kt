package com.ketabs

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ketabs.repository.ElementRepository
import com.ketabs.repository.InMemoryElementRepository
import com.ketabs.repository.InMemoryUserRepository
import com.ketabs.repository.UserRepository
import com.ketabs.route.auth
import com.ketabs.route.elements
import com.ketabs.service.makeCreateElement
import com.ketabs.service.makeCreateElementWithParent
import com.ketabs.service.makeFindElement
import com.ketabs.service.makeLoginAuth
import com.ketabs.service.makeRegisterAuth
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.features.ContentNegotiation
import io.ktor.routing.IgnoreTrailingSlash
import io.ktor.routing.routing
import io.ktor.serialization.json
import kotlinx.serialization.json.Json

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {

    val secret = environment.config.property("ktor.jwt.secret").getString()
    val issuer = environment.config.property("ktor.jwt.issuer").getString()
    val audience = environment.config.property("ktor.jwt.audience").getString()

    install(IgnoreTrailingSlash)
    install(ContentNegotiation) {
        json(
            Json { ignoreUnknownKeys = true }
        )
    }
    install(Authentication) {
        val myReal = environment.config.property("ktor.jwt.realm").getString()
        jwt("auth-jwt") {
            realm = myReal
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                when (credential.payload.getClaim("email").asString()) {
                    "" -> null
                    else -> JWTPrincipal(credential.payload)
                }
            }
        }
    }

    elementRoutes()
    authRoutes(secret, issuer, audience)
}

fun Application.elementRoutes() {
    val repo: ElementRepository = InMemoryElementRepository()
    routing {
        authenticate("auth-jwt") {
            elements(
                makeFindElement(repo),
                makeCreateElement(repo),
                makeCreateElementWithParent(repo),
            )
        }
    }
}

fun Application.authRoutes(secret: String, issuer: String, audience: String) {
    routing {
        val repo: UserRepository = InMemoryUserRepository()

        auth(
            makeLoginAuth(repo),
            makeRegisterAuth(repo),
            secret, issuer, audience
        )
    }
}
