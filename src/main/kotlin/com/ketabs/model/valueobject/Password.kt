package com.ketabs.model.valueobject

import arrow.core.Either
import org.mindrot.jbcrypt.BCrypt

sealed class Password private constructor() {
    abstract val value: String

    sealed class InvalidPassword(override val message: String) : IllegalArgumentException(message) {
        object TooShort : InvalidPassword(message = "Password must be more than 7 chars")
        object TooSimple :
            InvalidPassword(message = "Password must contains at least one number, one alphabetical uppercase char, and one special char")

        override fun toString() = message
    }

    companion object {
        private val FORMAT_REGEX =
            """^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@${'$'}%^&*-]).{8,}${'$'}""".toRegex()

        internal fun String.isComplex(): Boolean = FORMAT_REGEX.matches(this)
    }

    data class EncryptedPassword internal constructor(override val value: String) : Password() {
        companion object {
            fun of(s: String): Either<InvalidPassword, EncryptedPassword> = when {
                s.length < 7 -> Either.Left(InvalidPassword.TooShort)
                !s.isComplex() -> Either.Left(InvalidPassword.TooSimple)
                else -> Either.Right(EncryptedPassword(BCrypt.hashpw(s, BCrypt.gensalt())))
            }
        }

        override fun toString() = value

        fun matches(p: PlainPassword) = BCrypt.checkpw(p.value, this.value)
    }

    data class PlainPassword internal constructor(override val value: String) : Password() {
        companion object {
            fun of(s: String): Either<InvalidPassword, PlainPassword> = when {
                s.length < 7 -> Either.Left(InvalidPassword.TooShort)
                !s.isComplex() -> Either.Left(InvalidPassword.TooSimple)
                else -> Either.Right(PlainPassword(s))
            }
        }
    }
}
