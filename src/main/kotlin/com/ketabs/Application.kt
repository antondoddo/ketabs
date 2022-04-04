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

fun Application.module(
    userRepo: UserRepository = userRepository(),
    elementRepo: ElementRepository = elementRepository(),
    jwtConfig: JWTConfig = JWTConfig(),
) {
    install(IgnoreTrailingSlash)
    install(ContentNegotiation) {
        json(
            Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
                isLenient = true
            }
        )
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
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

    elementRoutes(elementRepo)
    authRoutes(userRepo, jwtConfig)
}

fun elementRepository(): ElementRepository = InMemoryElementRepository()

fun Application.elementRoutes(repo: ElementRepository) {
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

fun userRepository(): UserRepository = InMemoryUserRepository()

fun Application.authRoutes(
    repo: UserRepository,
    jwtConfig: JWTConfig
) {
    routing {
        auth(
            makeLoginAuth(repo),
            makeRegisterAuth(repo),
            jwtConfig.secret,
            jwtConfig.issuer,
            jwtConfig.audience,
        )
    }
}

data class JWTConfig(val secret: String, val issuer: String, val audience: String, val realm: String)

fun Application.JWTConfig(): JWTConfig {
    return JWTConfig(
        environment.config.property("ktor.jwt.secret").getString(),
        environment.config.property("ktor.jwt.issuer").getString(),
        environment.config.property("ktor.jwt.audience").getString(),
        environment.config.property("ktor.jwt.realm").getString(),
    )
}