package com.ketabs.service

import arrow.core.Either
import com.ketabs.model.Element
import com.ketabs.model.valueobject.ID
import com.ketabs.repository.ElementRepoReadError
import com.ketabs.repository.ElementRepository

data class FindElementData(val id: ID)

typealias FindElement = suspend (FindElementData) -> Either<FindElementError, Element>

sealed class FindElementError(val msg: String) {
    class ElementNotFound : FindElementError("Element was not found")
    class ReadError : FindElementError("Element was not read due to an error")
}

fun makeFindElement(repo: ElementRepository): FindElement {
    return { data: FindElementData ->
        repo.getBydID(data.id)
            .let {
                when (it) {
                    is Either.Right -> return@let Either.Right(it.value)
                    is Either.Left -> it.value
                }.let {
                    when (it) {
                        is ElementRepoReadError.InvalidReadElement -> Either.Left(FindElementError.ReadError())
                        is ElementRepoReadError.ElementNotFound -> Either.Left(FindElementError.ElementNotFound())
                    }
                }
            }
    }
}