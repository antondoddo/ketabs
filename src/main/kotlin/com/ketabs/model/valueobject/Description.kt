package com.ketabs.model.valueobject

import arrow.core.Either

@JvmInline
value class Description private constructor(private val s: String) {
    sealed class InvalidDescription(private val msg: String) : IllegalArgumentException(msg) {
        companion object {
            private const val TOO_LONG = "Description must not be longer than 200 chars"
        }

        class TooLong : InvalidDescription(TOO_LONG)

        override fun toString() = msg
    }

    companion object {
        fun of(s: String): Either<InvalidDescription, Description> = when {
            s.length <= 200 -> Either.Right(Description(s))
            else -> Either.Left(InvalidDescription.TooLong())
        }
    }

    override fun toString(): String {
        return s
    }

    fun isEqual(other: Any?): Boolean {
        return when (other) {
            null -> false
            is Description -> other.s == this.s
            else -> false
        }
    }
}
