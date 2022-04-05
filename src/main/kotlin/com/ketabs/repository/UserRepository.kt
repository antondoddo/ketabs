package com.ketabs.repository

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import com.ketabs.model.User
import com.ketabs.model.valueobject.Email
import com.ketabs.model.valueobject.ID
import java.util.concurrent.ConcurrentHashMap

sealed class UserRepoWriteError(val message: String) {
    object InvalidWriteUser : UserRepoWriteError("User was not written due to an error")
}

sealed class UserRepoReadError(override val message: String) : Exception(message) {
    object UserNotFound : UserRepoReadError("User was not found")
    object InvalidReadUser : UserRepoReadError("User was not read due to an error")
}

interface UserRepository {
    suspend fun getByID(id: ID): Either<UserRepoReadError, User>
    suspend fun getByEmail(email: Email): Either<UserRepoReadError, User>
    suspend fun add(user: User): Option<UserRepoWriteError>
}

class InMemoryUserRepository : UserRepository {
    private var store = ConcurrentHashMap<ID, User>()

    companion object {
        fun withStore(store: ConcurrentHashMap<ID, User>): InMemoryUserRepository {
            val repo = InMemoryUserRepository()
            repo.store = store
            return repo
        }
    }

    override suspend fun getByID(id: ID): Either<UserRepoReadError, User> {
        return when (val user = store.asSequence().filter { it.value.id == id }.firstOrNull()) {
            null -> Either.Left(UserRepoReadError.UserNotFound)
            else -> Either.Right(user.value)
        }
    }

    override suspend fun getByEmail(email: Email): Either<UserRepoReadError, User> {
        return when (val user = store.asSequence().filter { it.value.email == email }.firstOrNull()) {
            null -> Either.Left(UserRepoReadError.UserNotFound)
            else -> Either.Right(user.value)
        }
    }

    override suspend fun add(user: User): Option<UserRepoWriteError> {
        store[user.id] = user
        return None
    }
}
