package com.ketabs.repository

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import com.ketabs.model.User
import com.ketabs.model.valueobject.Email
import com.ketabs.model.valueobject.ID
import java.util.concurrent.ConcurrentHashMap


sealed class UserRepoWriteError(val msg: String) {
    class InvalidWriteUser : UserRepoWriteError("User was not written due to an error")
}

sealed class UserRepoReadError(val msg: String) {
    class UserNotFound : UserRepoReadError("User was not found")
    class InvalidReadUser : UserRepoReadError("User was not read due to an error")
}

interface UserRepository {
    suspend fun getByEmail(email: Email): Either<UserRepoReadError, User>
    suspend fun add(add: User): Option<UserRepoWriteError>
}

class InMemoryUserRepository() : UserRepository {
    private val store: MutableMap<ID, User> = ConcurrentHashMap()

    override suspend fun getByEmail(email: Email): Either<UserRepoReadError, User> {
        return when (val user = store.asSequence().filter { it.value.email.isEqual(email) }.firstOrNull()) {
            null -> Either.Left(UserRepoReadError.UserNotFound())
            else -> Either.Right(user.value)
        }
    }

    override suspend fun add(user: User): Option<UserRepoWriteError> {
        store[user.id] = user
        return None
    }
}
