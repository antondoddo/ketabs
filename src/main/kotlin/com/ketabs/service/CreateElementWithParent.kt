package com.ketabs.service

import arrow.core.Either
import arrow.core.Option
import com.ketabs.model.Element
import com.ketabs.model.valueobject.Description
import com.ketabs.model.valueobject.ID
import com.ketabs.model.valueobject.Link
import com.ketabs.model.valueobject.Name
import com.ketabs.repository.ElementRepoReadError
import com.ketabs.repository.ElementRepoWriteError
import com.ketabs.repository.ElementRepository

sealed class CreateElementWithParentData {
    abstract val name: Name
    abstract val description: Description
    abstract val userID: ID
    abstract val parentID: ID

    data class CreateTabData internal constructor(
        override val name: Name,
        override val description: Description,
        override val userID: ID,
        val link: Link,
        override val parentID: ID,
    ) : CreateElementWithParentData()

    data class CreateCollectionData internal constructor(
        override val name: Name,
        override val description: Description,
        override val userID: ID,
        override val parentID: ID,
    ) : CreateElementWithParentData()
}

sealed class CreateElementWithParentError(override val message: String) : Exception(message) {
    object ElementNotFound : CreateElementWithParentError("Element was not found")
    object ReadError : CreateElementWithParentError("Element was not read due to an error")
    object InvalidParentElement : CreateElementWithParentError("Element parent must be a collection")
    object InvalidOwner : CreateElementWithParentError("Element can't be created due to invalid permission")
    object WriteError : CreateElementWithParentError("Element was not written due to an error")
}

typealias CreateElementWithParent = suspend (CreateElementWithParentData) -> Either<CreateElementWithParentError, Element>

fun makeCreateElementWithParent(repo: ElementRepository): CreateElementWithParent {
    val errorHandler = { option: Option<ElementRepoWriteError>, element: Element ->
        option
            .toEither { element }
            .swap()
            .mapLeft {
                when (it) {
                    is ElementRepoWriteError.InvalidWriteElement -> CreateElementWithParentError.WriteError
                }
            }
    }

    return { data: CreateElementWithParentData ->
        with(repo.getByID(data.parentID)) {
            let {
                when (it) {
                    is Either.Left -> return@with when (it.value) {
                        is ElementRepoReadError.ElementNotFound -> Either.Left(CreateElementWithParentError.ElementNotFound)
                        is ElementRepoReadError.InvalidReadElement -> Either.Left(CreateElementWithParentError.ReadError)
                    }
                    is Either.Right -> it.value
                }
            }.let {
                when (it) {
                    is Element.Tab -> return@with Either.Left(CreateElementWithParentError.InvalidParentElement)
                    is Element.Collection -> it
                }
            }.let {
                when (it.canBeEditedBy(data.userID)) {
                    false -> return@with Either.Left(CreateElementWithParentError.InvalidOwner)
                    true -> it
                }
            }.let {
                when (data) {
                    is CreateElementWithParentData.CreateCollectionData -> {
                        val element = Element.Collection.createWithParent(data.name, data.description, it)
                        return@with errorHandler(repo.add(element), element)
                    }
                    is CreateElementWithParentData.CreateTabData -> {
                        val element = Element.Tab.createWithParent(data.name, data.description, data.link, it)
                        return@with errorHandler(repo.add(element), element)
                    }
                }
            }
        }
    }
}
