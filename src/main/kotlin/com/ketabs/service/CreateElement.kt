package com.ketabs.service

import arrow.core.Either
import arrow.core.Option
import com.ketabs.model.Element
import com.ketabs.model.valueobject.Description
import com.ketabs.model.valueobject.ID
import com.ketabs.model.valueobject.Link
import com.ketabs.model.valueobject.Name
import com.ketabs.repository.ElementRepoWriteError
import com.ketabs.repository.ElementRepository

sealed class CreateElementData {
    abstract val name: Name
    abstract val description: Description
    abstract val creatorID: ID

    data class CreateTabData internal constructor(
        override val name: Name,
        override val description: Description,
        override val creatorID: ID,
        val link: Link,
    ) : CreateElementData()

    data class CreateCollectionData internal constructor(
        override val name: Name,
        override val description: Description,
        override val creatorID: ID,
    ) : CreateElementData()
}

typealias CreateElement = suspend (CreateElementData) -> Either<CreateElementError, Element>

sealed class CreateElementError(override val message: String) : Exception(message) {
    object WriteError : CreateElementError("Element was not written due to an error")
}

fun makeCreateElement(repo: ElementRepository): CreateElement {
    val errorHandler = { option: Option<ElementRepoWriteError>, element: Element ->
        option
            .toEither { element }
            .swap()
            .mapLeft {
                when (it) {
                    is ElementRepoWriteError.InvalidWriteElement -> CreateElementError.WriteError
                }
            }
    }

    return { data: CreateElementData ->
        when (data) {
            is CreateElementData.CreateCollectionData -> {
                val element = Element.Collection.create(data.creatorID, data.name, data.description)
                errorHandler(repo.add(element), element)
            }
            is CreateElementData.CreateTabData -> {
                val element = Element.Tab.create(data.creatorID, data.name, data.description, data.link)
                errorHandler(repo.add(element), element)
            }
        }
    }
}
