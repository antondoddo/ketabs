package com.ketabs.route.request

import com.ketabs.model.valueobject.ID
import com.ketabs.service.FindElementData

@kotlinx.serialization.Serializable
data class FindElementRequest(val id: String) {
    fun parse() = parse(
        Pair("id", ID.of(id))
    ) { id -> FindElementData(id) }
}
