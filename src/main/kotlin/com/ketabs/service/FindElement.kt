package com.ketabs.service

import arrow.core.Either
import com.ketabs.model.Element
import com.ketabs.model.valueobject.ID
import com.ketabs.repository.ElementRepoReadError
import com.ketabs.repository.ElementRepository

data class FindElementData(val id: ID)

typealias FindElement = suspend (FindElementData) -> Either<FindElementError, Element>

sealed class FindElementError(override val message: String) : Exception(message) {
    object ElementNotFound : FindElementError("Element was not found")
    object ReadError : FindElementError("Element was not read due to an error")
}

fun makeFindElement(repo: ElementRepository): FindElement {
    return { data: FindElementData ->
        repo.getByID(data.id)
            .mapLeft {
                when (it) {
                    is ElementRepoReadError.InvalidReadElement -> FindElementError.ReadError
                    is ElementRepoReadError.ElementNotFound -> FindElementError.ElementNotFound
                }
            }
    }
}
