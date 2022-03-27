package com.ketabs.model.valueobject

import arrow.core.Either

@JvmInline
value class Link private constructor(private val s: String) {
    sealed class InvalidLink(private val msg: String) : IllegalArgumentException(msg) {
        companion object {
            private const val INVALID_FORMAT = "Link must be a valid URL"
        }

        class InvalidFormat : InvalidLink(INVALID_FORMAT)

        override fun toString() = msg
    }

    companion object {

        private val FORMAT_REGEX =
            """(http|ftp|https):\/\/([\w_-]+(?:(?:\.[\w_-]+)+))([\w.,@?^=%&:\/~+#-]*[\w@?^=%&\/~+#-])""".toRegex()

        fun of(s: String): Either<InvalidLink, Link> = when {
            s.isUrl() -> Either.Right(Link(s))
            else -> Either.Left(InvalidLink.InvalidFormat())
        }

        private fun String.isUrl(): Boolean = FORMAT_REGEX.matches(this)
    }

    override fun toString(): String = s

    fun isEqual(other: Any?): Boolean {
        return when (other) {
            null -> false
            is Link -> other.s == this.s
            else -> false
        }
    }
}
