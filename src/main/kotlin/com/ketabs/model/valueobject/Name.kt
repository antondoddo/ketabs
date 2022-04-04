package com.ketabs.model.valueobject

import arrow.core.Either

@JvmInline
value class Name private constructor(val value: String) {
    sealed class InvalidName(private val msg: String) : IllegalArgumentException(msg) {
        companion object {
            private const val TOO_LONG = "Name must be at max 100 chars"
            private const val TOO_SHORT = "Name must be at least 2 chars"
        }

        object TooLong : InvalidName(TOO_LONG)
        object TooShort : InvalidName(TOO_SHORT)

        override fun toString() = msg
    }

    companion object {
        fun of(s: String): Either<InvalidName, Name> = when {
            s.length < 2 -> Either.Left(InvalidName.TooShort)
            s.length > 100 -> Either.Left(InvalidName.TooLong)
            else -> Either.Right(Name(s))
        }
    }
}
