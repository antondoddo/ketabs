package com.ketabs.route.request

import com.ketabs.model.valueobject.Email
import com.ketabs.model.valueobject.Password
import com.ketabs.service.LoginAuthData

@kotlinx.serialization.Serializable
data class LoginAuthRequest(
    val email: String = "",
    @kotlinx.serialization.SerialName("password") val plainPassword: String = "",
) {
    fun parse() = parse(
        "email" to Email.of(email),
        "password" to Password.PlainPassword.of(plainPassword),
    ) { email, plainPassword -> LoginAuthData(email, plainPassword) }
}
