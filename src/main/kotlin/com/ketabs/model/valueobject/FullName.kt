package com.ketabs.model.valueobject

import arrow.core.Either

@JvmInline
value class FullName private constructor(val value: String) {
    sealed class InvalidFullName(override val message: String) : IllegalArgumentException(message) {
        object TooShort : InvalidFullName(message = "Full name must be more than 5 chars")

        override fun toString() = message
    }

    companion object {
        fun of(s: String) = when {
            s.length < 5 -> Either.Left(InvalidFullName.TooShort)
            else -> Either.Right(FullName(s))
        }
    }
}
