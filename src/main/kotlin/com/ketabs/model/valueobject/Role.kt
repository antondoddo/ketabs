package com.ketabs.model.valueobject

enum class Role {
    ADMIN,
    EDITOR,
    VIEWER;

    fun canBeAdministrated(): Boolean {
        return this == ADMIN
    }

    fun canBeEdited(): Boolean {
        return this == ADMIN || this == EDITOR
    }

    fun canBeViewed(): Boolean {
        return this == ADMIN || this == EDITOR || this == VIEWER
    }
}