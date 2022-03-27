package com.ketabs.route.request

import com.ketabs.model.valueobject.Description
import com.ketabs.model.valueobject.ID
import com.ketabs.model.valueobject.Link
import com.ketabs.model.valueobject.Name
import com.ketabs.service.CreateElementData
import com.ketabs.service.CreateElementWithParentData

@kotlinx.serialization.Serializable
data class CreateElementWithParentRequest(
    val name: String = "",
    val description: String = "",
    val link: String? = null,
    var parentID: String = "",
) {
    fun parse() = when {
        !link.isNullOrBlank() ->
            parse(
                Pair("name", Name.of(name)),
                Pair("description", Description.of(description)),
                Pair("link", Link.of(link)),
                Pair("id", ID.of(parentID)),
            ) { name, description, link, parentID ->
                CreateElementWithParentData.CreateTabData(
                    name,
                    description,
                    link,
                    parentID
                )
            }
        else ->
            parse(
                Pair("name", Name.of(name)),
                Pair("description", Description.of(description)),
                Pair("id", ID.of(parentID)),
            ) { name, description, parentID ->
                CreateElementWithParentData.CreateCollectionData(
                    name,
                    description,
                    parentID
                )
            }
    }
}
