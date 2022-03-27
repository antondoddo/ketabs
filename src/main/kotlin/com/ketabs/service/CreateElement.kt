package com.ketabs.service

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.ketabs.model.Element
import com.ketabs.model.valueobject.Description
import com.ketabs.model.valueobject.Link
import com.ketabs.model.valueobject.Name
import com.ketabs.repository.ElementRepoWriteError
import com.ketabs.repository.ElementRepository

sealed class CreateElementData {
    abstract val name: Name
    abstract val description: Description

    data class CreateTabData internal constructor(
        override val name: Name,
        override val description: Description,
        val link: Link,
    ) : CreateElementData()

    data class CreateCollectionData internal constructor(
        override val name: Name,
        override val description: Description,
    ) : CreateElementData()
}

typealias CreateElement = suspend (CreateElementData) -> Either<CreateElementError, Element>

sealed class CreateElementError(val msg: String) {
    class WriteError : CreateElementError("Element was not written due to an error")
}

fun makeCreateElement(repo: ElementRepository): CreateElement {
    val errorHandler = { option: Option<ElementRepoWriteError>, element: Element ->
        when (option) {
            is None -> Either.Right(element)
            is Some -> when (option.value) {
                is ElementRepoWriteError.InvalidWriteElement -> Either.Left(CreateElementError.WriteError())
            }
        }
    }
    return { data: CreateElementData ->
        when (data) {
            is CreateElementData.CreateCollectionData -> {
                val element = Element.Collection.create(data.name, data.description)
                errorHandler(repo.add(element), element)
            }
            is CreateElementData.CreateTabData -> {
                val element = Element.Tab.create(data.name, data.description, data.link)
                errorHandler(repo.add(element), element)
            }
        }
    }
}
