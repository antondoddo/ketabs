package com.ketabs.route.request

import com.ketabs.model.valueobject.Description
import com.ketabs.model.valueobject.Link
import com.ketabs.model.valueobject.Name
import com.ketabs.service.CreateElementData

@kotlinx.serialization.Serializable
data class CreateElementRequest(
    val name: String = "",
    val description: String = "",
    val link: String? = null,
) {
    fun parse() = when {
        !link.isNullOrBlank() ->
            parse(
                Pair("name", Name.of(name)),
                Pair("description", Description.of(description)),
                Pair("link", Link.of(link)),
            ) { name, description, link -> CreateElementData.CreateTabData(name, description, link) }
        else ->
            parse(
                Pair("name", Name.of(name)),
                Pair("description", Description.of(description)),
            ) { name, description -> CreateElementData.CreateCollectionData(name, description) }
    }
}
