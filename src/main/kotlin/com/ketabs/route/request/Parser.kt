package com.ketabs.route.request

import arrow.core.Either
import arrow.core.computations.ResultEffect.bind

typealias ValidationErrors = Map<String, String>
typealias KeyValueValidationEither<A> = Pair<String, Either<IllegalArgumentException, A>>

internal fun <A, R> parse(
    a: KeyValueValidationEither<A>,
    f: (A) -> R
): Either<ValidationErrors, R> = when {
    listOf(a).any { it.second.isLeft() } -> Either.Left(parseValidationError(listOf(a)))
    else -> Either.Right(f(a.second.bind()))
}

internal fun <A, B, R> parse(
    a: KeyValueValidationEither<A>,
    b: KeyValueValidationEither<B>,
    f: (A, B) -> R
): Either<ValidationErrors, R> = when {
    listOf(a, b).any { it.second.isLeft() } -> Either.Left(parseValidationError(listOf(a, b)))
    else -> Either.Right(f(a.second.bind(), b.second.bind()))
}

internal fun <A, B, C, R> parse(
    a: KeyValueValidationEither<A>,
    b: KeyValueValidationEither<B>,
    c: KeyValueValidationEither<C>,
    f: (A, B, C) -> R
): Either<ValidationErrors, R> = when {
    listOf(a, b, c).any { it.second.isLeft() } -> Either.Left(parseValidationError(listOf(a, b, c)))
    else -> Either.Right(f(a.second.bind(), b.second.bind(), c.second.bind()))
}

internal fun <A, B, C, D, R> parse(
    a: KeyValueValidationEither<A>,
    b: KeyValueValidationEither<B>,
    c: KeyValueValidationEither<C>,
    d: KeyValueValidationEither<D>,
    f: (A, B, C, D) -> R
): Either<ValidationErrors, R> = when {
    listOf(a, b, c, d).any { it.second.isLeft() } -> Either.Left(parseValidationError(listOf(a, b, c, d)))
    else -> Either.Right(f(a.second.bind(), b.second.bind(), c.second.bind(), d.second.bind()))
}

private fun parseValidationError(list: List<KeyValueValidationEither<Any?>>): ValidationErrors {
    return list.mapNotNull {
        when (val second = it.second) {
            is Either.Left -> Pair(it.first, second.value.toString())
            is Either.Right -> null
        }
    }.associate { it.first to it.second }
}
