package com.ketabs.model.valueobject

import arrow.core.Either

@JvmInline
value class Email private constructor(val value: String) {
    sealed class InvalidEmail(override val message: String) : IllegalArgumentException(message) {
        object InvalidFormat : InvalidEmail(message = "Email must be a valid email address")

        override fun toString() = message
    }

    companion object {

        private val FORMAT_REGEX =
            """(?:[a-z0-9!#${'$'}%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#${'$'}%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])""".toRegex()

        fun of(s: String): Either<InvalidEmail, Email> = when {
            s.isEmailAddress() -> Either.Right(Email(s))
            else -> Either.Left(InvalidEmail.InvalidFormat)
        }

        private fun String.isEmailAddress(): Boolean = FORMAT_REGEX.matches(this)
    }
}
