package com.ketabs.model.valueobject

import arrow.core.Either

@JvmInline
value class Name private constructor(val value: String) {
    sealed class InvalidName(override val message: String) : IllegalArgumentException(message) {
        object TooLong : InvalidName(message = "Name must be at max 100 chars")
        object TooShort : InvalidName(message = "Name must be at least 2 chars")

        override fun toString() = message
    }

    companion object {
        fun of(s: String): Either<InvalidName, Name> = when {
            s.length < 2 -> Either.Left(InvalidName.TooShort)
            s.length > 100 -> Either.Left(InvalidName.TooLong)
            else -> Either.Right(Name(s))
        }
    }
}
