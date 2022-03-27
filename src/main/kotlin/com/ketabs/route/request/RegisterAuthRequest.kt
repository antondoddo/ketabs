package com.ketabs.route.request

import com.ketabs.model.valueobject.Email
import com.ketabs.model.valueobject.FullName
import com.ketabs.model.valueobject.Password
import com.ketabs.service.RegisterAuthData

@kotlinx.serialization.Serializable
data class RegisterAuthRequest(
    val email: String = "",
    @kotlinx.serialization.SerialName("full_name") val fullName: String = "",
    @kotlinx.serialization.SerialName("password") val plainPassword: String = ""
) {
    fun parse() = parse(
        Pair("email", Email.of(email)),
        Pair("full_name", FullName.of(fullName)),
        Pair("password", Password.EncryptedPassword.of(plainPassword)),
    ) { email, fullName, password -> RegisterAuthData(email, fullName, password) }
}
