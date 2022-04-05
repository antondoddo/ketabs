package com.ketabs.route.request

import com.ketabs.model.valueobject.Description
import com.ketabs.model.valueobject.ID
import com.ketabs.model.valueobject.Link
import com.ketabs.model.valueobject.Name
import com.ketabs.service.CreateElementWithParentData

@kotlinx.serialization.Serializable
data class CreateElementWithParentRequest(
    val name: String = "",
    val description: String = "",
    val link: String? = null,
    var parentID: String = "",
) {
    fun parse(userID: ID) = when {
        !link.isNullOrBlank() ->
            parse(
                "name" to Name.of(name),
                "description" to Description.of(description),
                "link" to Link.of(link),
                "id" to ID.of(parentID),
            ) { name, description, link, parentID ->
                CreateElementWithParentData.CreateTabData(
                    name,
                    description,
                    userID,
                    link,
                    parentID
                )
            }
        else ->
            parse(
                "name" to Name.of(name),
                "description" to Description.of(description),
                "id" to ID.of(parentID),
            ) { name, description, parentID ->
                CreateElementWithParentData.CreateCollectionData(
                    name,
                    description,
                    userID,
                    parentID
                )
            }
    }
}
