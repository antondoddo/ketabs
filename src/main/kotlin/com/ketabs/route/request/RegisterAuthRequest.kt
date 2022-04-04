package com.ketabs.route.request

import com.ketabs.model.valueobject.Email
import com.ketabs.model.valueobject.FullName
import com.ketabs.model.valueobject.Password
import com.ketabs.service.RegisterAuthData

@kotlinx.serialization.Serializable
data class RegisterAuthRequest(
    val email: String = "",
    @kotlinx.serialization.SerialName("full_name") val fullName: String = "",
    val password: String = ""
) {
    fun parse() = parse(
        "email" to Email.of(email),
        "full_name" to FullName.of(fullName),
        "password" to Password.EncryptedPassword.of(password),
    ) { email, fullName, password -> RegisterAuthData(email, fullName, password) }
}
