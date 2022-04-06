package com.ketabs.service

import arrow.core.Either
import arrow.core.Option
import com.ketabs.model.User
import com.ketabs.model.valueobject.Email
import com.ketabs.model.valueobject.FullName
import com.ketabs.model.valueobject.Password
import com.ketabs.repository.UserRepoWriteError
import com.ketabs.repository.UserRepository

data class RegisterAuthData(val email: Email, val fullName: FullName, val password: Password.EncryptedPassword)

typealias RegisterAuth = suspend (RegisterAuthData) -> Either<RegisterAuthError, User>

sealed class RegisterAuthError(override val message: String) : Exception(message) {
    object WriteError : RegisterAuthError("User was not written due to an error")
}

fun makeRegisterAuth(repo: UserRepository): RegisterAuth {
    val errorHandler = { option: Option<UserRepoWriteError>, user: User ->
        option
            .toEither { user }
            .swap()
            .mapLeft {
                when (it) {
                    is UserRepoWriteError.InvalidWriteUser -> RegisterAuthError.WriteError
                }
            }
    }

    return { data: RegisterAuthData ->
        val user = User.create(data.email, data.fullName, data.password)
        errorHandler(repo.add(user), user)
    }
}
