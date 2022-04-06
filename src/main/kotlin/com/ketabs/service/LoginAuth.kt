package com.ketabs.service

import arrow.core.Either
import arrow.core.ensure
import com.ketabs.model.User
import com.ketabs.model.valueobject.Email
import com.ketabs.model.valueobject.Password
import com.ketabs.repository.UserRepoReadError
import com.ketabs.repository.UserRepository

data class LoginAuthData(val email: Email, val plainPassword: Password.PlainPassword)

typealias LoginAuth = suspend (LoginAuthData) -> Either<LoginAuthError, User>

sealed class LoginAuthError(override val message: String) : Exception(message) {
    object UserNotFound : LoginAuthError("User was not authorized")
    object ReadError : LoginAuthError("User was not read due to an error")
}

fun makeLoginAuth(repo: UserRepository): LoginAuth {
    return { data: LoginAuthData ->
        repo.getByEmail(data.email)
            .mapLeft {
                when (it) {
                    is UserRepoReadError.InvalidReadUser -> LoginAuthError.ReadError
                    is UserRepoReadError.UserNotFound -> LoginAuthError.UserNotFound
                }
            }.ensure(
                predicate = { it.password.matches(data.plainPassword) },
                error = { LoginAuthError.UserNotFound },
            )
    }
}
