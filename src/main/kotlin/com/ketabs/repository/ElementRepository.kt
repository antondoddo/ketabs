package com.ketabs.repository

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import com.ketabs.model.Element
import com.ketabs.model.valueobject.ID
import java.util.concurrent.ConcurrentHashMap

sealed class ElementRepoWriteError(val message: String) {
    object InvalidWriteElement : ElementRepoWriteError("Element was not written due to an error")
}

sealed class ElementRepoReadError(val message: String) {
    object ElementNotFound : ElementRepoReadError("Element was not found")
    object InvalidReadElement : ElementRepoReadError("Element was not read due to an error")
}

interface ElementRepository {
    suspend fun getByID(id: ID): Either<ElementRepoReadError, Element>
    suspend fun add(element: Element): Option<ElementRepoWriteError>
}

class InMemoryElementRepository() : ElementRepository {
    private val store = ConcurrentHashMap<ID, Element>()

    override suspend fun getByID(id: ID): Either<ElementRepoReadError, Element> =
        when (val element = store[id]) {
            null -> Either.Left(ElementRepoReadError.ElementNotFound)
            else -> Either.Right(element)
        }

    override suspend fun add(element: Element): Option<ElementRepoWriteError> {
        store[element.id] = element
        return None
    }
}
