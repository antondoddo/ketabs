package com.ketabs.model.valueobject

import arrow.core.Either

@JvmInline
value class Description private constructor(val value: String) {
    sealed class InvalidDescription(override val message: String) : IllegalArgumentException(message) {
        object TooLong : InvalidDescription(message = "Description must not be longer than 200 chars")

        override fun toString() = message
    }

    companion object {
        fun of(s: String): Either<InvalidDescription, Description> = when {
            s.length <= 200 -> Either.Right(Description(s))
            else -> Either.Left(InvalidDescription.TooLong)
        }
    }
}
