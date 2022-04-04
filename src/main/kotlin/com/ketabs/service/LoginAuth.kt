package com.ketabs.service

import arrow.core.Either
import com.ketabs.model.User
import com.ketabs.model.valueobject.Email
import com.ketabs.model.valueobject.Password
import com.ketabs.repository.UserRepoReadError
import com.ketabs.repository.UserRepository

data class LoginAuthData(val email: Email, val plainPassword: Password.PlainPassword)

typealias LoginAuth = suspend (LoginAuthData) -> Either<LoginAuthError, User>

sealed class LoginAuthError(val msg: String) {
    object UserNotFound : LoginAuthError("User was not authorized")
    object ReadError : LoginAuthError("User was not read due to an error")
}

fun makeLoginAuth(repo: UserRepository): LoginAuth {
    return { data: LoginAuthData ->
        repo.getByEmail(data.email).let {
            when (it) {
                is Either.Right -> when (it.value.password.matches(data.plainPassword)) {
                    true -> return@let Either.Right(it.value)
                    false -> return@let Either.Left(LoginAuthError.UserNotFound)
                }
                is Either.Left -> it.value
            }.let {
                when (it) {
                    is UserRepoReadError.InvalidReadUser -> Either.Left(LoginAuthError.ReadError)
                    is UserRepoReadError.UserNotFound -> Either.Left(LoginAuthError.UserNotFound)
                }
            }
        }
    }
}
