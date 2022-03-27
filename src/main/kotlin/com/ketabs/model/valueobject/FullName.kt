package com.ketabs.model.valueobject

import arrow.core.Either

@JvmInline
value class FullName private constructor(private val s: String) {
    sealed class InvalidFullName(private val msg: String) : IllegalArgumentException(msg) {
        companion object {
            private const val TOO_SHORT = "Full name must be more than 5 chars"
        }

        class TooShort : InvalidFullName(TOO_SHORT)

        override fun toString() = msg
    }

    companion object {
        fun of(s: String) = when {
            s.length < 5 -> Either.Left(InvalidFullName.TooShort())
            else -> Either.Right(FullName(s))
        }
    }

    override fun toString(): String = s

    fun isEqual(other: Any?): Boolean {
        return when (other) {
            null -> false
            is FullName -> other.s == this.s
            else -> false
        }
    }
}
