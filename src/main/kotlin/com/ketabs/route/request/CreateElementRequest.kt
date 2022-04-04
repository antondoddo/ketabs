package com.ketabs.route.request

import com.ketabs.model.valueobject.Description
import com.ketabs.model.valueobject.ID
import com.ketabs.model.valueobject.Link
import com.ketabs.model.valueobject.Name
import com.ketabs.service.CreateElementData

@kotlinx.serialization.Serializable
data class CreateElementRequest(
    val name: String = "",
    val description: String = "",
    val link: String? = null,
) {
    fun parse(id: ID) = when {
        !link.isNullOrBlank() ->
            parse(
                "name" to Name.of(name),
                "description" to Description.of(description),
                "link" to Link.of(link),
            ) { name, description, link -> CreateElementData.CreateTabData(name, description, id, link) }
        else ->
            parse(
                "name" to Name.of(name),
                "description" to Description.of(description),
            ) { name, description -> CreateElementData.CreateCollectionData(name, description, id) }
    }
}
