package com.ketabs.model

import com.ketabs.model.valueobject.Description
import com.ketabs.model.valueobject.ID
import com.ketabs.model.valueobject.Link
import com.ketabs.model.valueobject.Name
import java.time.LocalDateTime

sealed class Element private constructor() {
    abstract val id: ID
    abstract val name: Name
    abstract val description: Description
    abstract val parent: Collection?
    abstract val createdAt: LocalDateTime
    abstract val updatedAt: LocalDateTime?
    abstract val trashedAt: LocalDateTime?

    data class Tab internal constructor(
        override val id: ID,
        override val name: Name,
        override val description: Description,
        val link: Link?,
        override val parent: Collection?,
        override val createdAt: LocalDateTime,
        override val updatedAt: LocalDateTime? = null,
        override val trashedAt: LocalDateTime? = null,
    ) : Element() {
        companion object {
            fun create(name: Name, description: Description, link: Link) =
                Tab(
                    ID.random(),
                    name,
                    description,
                    link,
                    null,
                    LocalDateTime.now(),
                )

            fun createWithParent(name: Name, description: Description, link: Link, parent: Collection) =
                Tab(
                    ID.random(),
                    name,
                    description,
                    link,
                    parent,
                    LocalDateTime.now(),
                )
        }

        fun withLink(l: Link) = this.copy(link = l)
    }

    data class Collection internal constructor(
        override val id: ID,
        override val name: Name,
        override val description: Description,
        override val parent: Collection?,
        override val createdAt: LocalDateTime,
        override val updatedAt: LocalDateTime? = null,
        override val trashedAt: LocalDateTime? = null,
    ) : Element() {
        companion object {
            fun create(name: Name, description: Description) =
                Collection(
                    ID.random(),
                    name,
                    description,
                    null,
                    LocalDateTime.now(),
                )

            fun createWithParent(name: Name, description: Description, parent: Collection) =
                Collection(
                    ID.random(),
                    name,
                    description,
                    parent,
                    LocalDateTime.now(),
                )
        }
    }

    fun withName(n: Name) = when (this) {
        is Tab -> this.copy(name = n)
        is Collection -> this.copy(name = n)
    }

    fun withDescription(d: Description) = when (this) {
        is Tab -> this.copy(description = d)
        is Collection -> this.copy(description = d)
    }

    fun hasParent(): Boolean = parent != null
}
