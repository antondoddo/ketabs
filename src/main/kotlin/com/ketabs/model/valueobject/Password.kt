package com.ketabs.model.valueobject

import arrow.core.Either
import org.mindrot.jbcrypt.BCrypt

sealed class Password private constructor() {
    abstract val s: String

    sealed class InvalidPassword(private val msg: String) : IllegalArgumentException(msg) {
        companion object {
            private const val TOO_SHORT = "Password must be more than 7 chars"
            private const val TOO_SIMPLE =
                "Password must contains at least one number, one alphabetical uppercase char, and one special char"
        }

        class TooShort : InvalidPassword(TOO_SHORT)
        class TooSimple : InvalidPassword(TOO_SIMPLE)

        override fun toString() = msg
    }

    companion object {
        private val FORMAT_REGEX =
            """^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@${'$'}%^&*-]).{8,}${'$'}""".toRegex()

        internal fun String.isComplex(): Boolean = FORMAT_REGEX.matches(this)
    }

    data class EncryptedPassword internal constructor(override val s: String) : Password() {
        companion object {
            fun of(s: String): Either<InvalidPassword, EncryptedPassword> = when {
                s.length < 7 -> Either.Left(InvalidPassword.TooShort())
                !s.isComplex() -> Either.Left(InvalidPassword.TooSimple())
                else -> Either.Right(EncryptedPassword(BCrypt.hashpw(s, BCrypt.gensalt())))
            }
        }

        override fun toString() = s

        fun matches(p: PlainPassword) = BCrypt.checkpw(p.toString(), this.s)
    }

    data class PlainPassword internal constructor(override val s: String) : Password() {
        companion object {
            fun of(s: String): Either<InvalidPassword, PlainPassword> = when {
                s.length < 7 -> Either.Left(InvalidPassword.TooShort())
                !s.isComplex() -> Either.Left(InvalidPassword.TooSimple())
                else -> Either.Right(PlainPassword(s))
            }
        }

        override fun toString() = s
    }
}