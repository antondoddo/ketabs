package com.ketabs.model

import com.ketabs.model.valueobject.Email
import com.ketabs.model.valueobject.FullName
import com.ketabs.model.valueobject.ID
import com.ketabs.model.valueobject.Password

data class User internal constructor(
    val id: ID,
    val email: Email,
    val fullName: FullName,
    val password: Password.EncryptedPassword,
) {
    companion object {
        fun create(email: Email, fullName: FullName, password: Password.EncryptedPassword) =
            User(
                ID.random(),
                email,
                fullName,
                password,
            )
    }

    fun withFullName(fn: FullName) = this.copy(fullName = fn)
}
