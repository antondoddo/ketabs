package com.ketabs.model.valueobject

import arrow.core.Either

@JvmInline
value class Email private constructor(private val s: String) {
    sealed class InvalidEmail(private val msg: String) : IllegalArgumentException(msg) {
        companion object {
            private const val INVALID_FORMAT = "Email must be a valid email address"
        }

        class InvalidFormat : InvalidEmail(INVALID_FORMAT)

        override fun toString() = msg
    }

    companion object {

        private val FORMAT_REGEX =
            """(?:[a-z0-9!#${'$'}%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#${'$'}%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])""".toRegex()

        fun of(s: String) = when {
            s.isEmailAddress() -> Either.Right(Email(s))
            else -> Either.Left(InvalidEmail.InvalidFormat())
        }

        private fun String.isEmailAddress(): Boolean = FORMAT_REGEX.matches(this)
    }

    override fun toString(): String = s

    fun isEqual(other: Any?): Boolean {
        return when (other) {
            null -> false
            is Email -> other.s == this.s
            else -> false
        }
    }
}
