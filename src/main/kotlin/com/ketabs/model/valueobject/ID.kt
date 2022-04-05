package com.ketabs.model.valueobject

import arrow.core.Either
import java.util.UUID

@JvmInline
value class ID private constructor(val value: String) {
    sealed class InvalidID(override val message: String) : IllegalArgumentException(message) {
        object InvalidFormat : InvalidID(message = "ID must be a UUID")

        override fun toString() = message
    }

    companion object {
        fun random() = ID(UUID.randomUUID().toString())
        fun of(s: String) = when {
            s.isUUID() -> Either.Right(ID(s))
            else -> Either.Left(InvalidID.InvalidFormat)
        }

        private fun String.isUUID(): Boolean = try {
            UUID.fromString(this)
            true
        } catch (ex: Throwable) {
            false
        }
    }
}
