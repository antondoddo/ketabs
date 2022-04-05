package com.ketabs

import com.ketabs.repository.ElementRepository
import com.ketabs.repository.InMemoryElementRepository
import com.ketabs.repository.InMemoryUserRepository
import com.ketabs.repository.UserRepository
import io.ktor.application.Application

internal fun testApp(
    userRepo: UserRepository = InMemoryUserRepository(),
    elementRepo: ElementRepository = InMemoryElementRepository(),
    jwtConfig: JWTConfig = JWTConfig("example", "example", "example", "example"),
): Application.() -> Unit = {
    module(
        userRepo = userRepo,
        elementRepo = elementRepo,
        jwtConfig = jwtConfig,
    )
}
