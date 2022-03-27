package com.ketabs.model.valueobject

import arrow.core.Either
import java.util.UUID

@JvmInline
value class ID private constructor(private val s: String) {
    sealed class InvalidID(private val msg: String) : IllegalArgumentException(msg) {
        companion object {
            private const val INVALID_FORMAT = "ID must be a UUID"
        }

        class InvalidFormat : InvalidID(INVALID_FORMAT)

        override fun toString() = msg
    }

    companion object {
        fun random() = ID(UUID.randomUUID().toString())
        fun of(s: String) = when {
            s.isUUID() -> Either.Right(ID(s))
            else -> Either.Left(InvalidID.InvalidFormat())
        }

        private fun String.isUUID(): Boolean = try {
            UUID.fromString(this)
            true
        } catch (ex: Throwable) {
            false
        }
    }

    override fun toString(): String {
        return s
    }

    fun isEqual(other: Any?): Boolean {
        return when (other) {
            null -> false
            is ID -> other.s == this.s
            else -> false
        }
    }
}
