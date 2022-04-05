package com.ketabs.model.valueobject

import arrow.core.Either

@JvmInline
value class Link private constructor(val value: String) {
    sealed class InvalidLink(override val message: String) : IllegalArgumentException(message) {
        object InvalidFormat : InvalidLink(message = "Link must be a valid URL")

        override fun toString() = message
    }

    companion object {

        private val FORMAT_REGEX =
            """(http|ftp|https):\/\/([\w_-]+(?:(?:\.[\w_-]+)+))([\w.,@?^=%&:\/~+#-]*[\w@?^=%&\/~+#-])""".toRegex()

        fun of(s: String): Either<InvalidLink, Link> = when {
            s.isUrl() -> Either.Right(Link(s))
            else -> Either.Left(InvalidLink.InvalidFormat)
        }

        private fun String.isUrl(): Boolean = FORMAT_REGEX.matches(this)
    }
}
