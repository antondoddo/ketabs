package com.ketabs.model

import com.ketabs.model.valueobject.Description
import com.ketabs.model.valueobject.ID
import com.ketabs.model.valueobject.Link
import com.ketabs.model.valueobject.Name
import com.ketabs.model.valueobject.Owner
import com.ketabs.model.valueobject.Role
import java.time.LocalDateTime

sealed class Element private constructor() {
    abstract val id: ID
    abstract val name: Name
    abstract val description: Description
    abstract val owners: List<Owner>
    abstract val parent: Collection?
    abstract val createdAt: LocalDateTime
    abstract val updatedAt: LocalDateTime?
    abstract val trashedAt: LocalDateTime?

    data class Tab internal constructor(
        override val id: ID,
        override val name: Name,
        override val description: Description,
        val link: Link,
        override val owners: List<Owner>,
        override val parent: Collection?,
        override val createdAt: LocalDateTime,
        override val updatedAt: LocalDateTime? = null,
        override val trashedAt: LocalDateTime? = null,
    ) : Element() {
        companion object {
            fun create(creator: ID, name: Name, description: Description, link: Link) =
                Tab(
                    ID.random(),
                    name,
                    description,
                    link,
                    listOf(Owner(creator, Role.ADMIN)),
                    null,
                    LocalDateTime.now(),
                )

            fun createWithParent(name: Name, description: Description, link: Link, parent: Collection) =
                Tab(
                    ID.random(),
                    name,
                    description,
                    link,
                    parent.owners,
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
        override val owners: List<Owner>,
        override val parent: Collection?,
        override val createdAt: LocalDateTime,
        override val updatedAt: LocalDateTime? = null,
        override val trashedAt: LocalDateTime? = null,
    ) : Element() {
        companion object {
            fun create(creator: ID, name: Name, description: Description) =
                Collection(
                    ID.random(),
                    name,
                    description,
                    listOf(Owner(creator, Role.ADMIN)),
                    null,
                    LocalDateTime.now(),
                )

            fun createWithParent(name: Name, description: Description, parent: Collection) =
                Collection(
                    ID.random(),
                    name,
                    description,
                    parent.owners,
                    parent,
                    LocalDateTime.now(),
                )
        }
    }

    fun withoutOwner(id: ID) = when (this) {
        is Tab -> this.copy(owners = this.owners.filter { it.userID != id })
        is Collection -> this.copy(owners = this.owners.filter { it.userID != id })
    }

    fun withOwner(o: Owner) = when (this) {
        is Tab -> this.copy(owners = this.owners.plusElement(o))
        is Collection -> this.copy(owners = this.owners.plusElement(o))
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

    fun canBeEditedBy(userID: ID): Boolean = this.owners.any { it.userID == userID && it.role.canBeEdited() }
}
