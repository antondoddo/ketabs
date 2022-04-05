package com.ketabs

import arrow.core.computations.ResultEffect.bind
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ketabs.model.Element
import com.ketabs.model.User
import com.ketabs.model.valueobject.Description
import com.ketabs.model.valueobject.Email
import com.ketabs.model.valueobject.FullName
import com.ketabs.model.valueobject.ID
import com.ketabs.model.valueobject.Link
import com.ketabs.model.valueobject.Name
import com.ketabs.model.valueobject.Owner
import com.ketabs.model.valueobject.Password
import com.ketabs.model.valueobject.Role
import io.github.serpro69.kfaker.faker
import java.time.LocalDateTime
import java.util.Date

class ObjectMother {

    companion object {
        private val faker = faker { }

        fun randomID() = ID.random()

        fun randomName() = Name.of(faker.app.name()).bind()
        fun randomDescription() = Description.of(faker.book.title()).bind()
        fun randomLink() = Link.of(faker.siliconValley.urls()).bind()
        fun randomOwner() = Owner(randomID(), Role.ADMIN)
        fun randomOwners(len: Int = 5) = List(len) { randomOwner() }
        fun randomCollectionElement() =
            Element.Collection(
                randomID(),
                randomName(),
                randomDescription(),
                randomOwners(),
                null,
                LocalDateTime.now()
            )

        fun randomTabElement() =
            Element.Tab(
                randomID(),
                randomName(),
                randomDescription(),
                randomLink(),
                randomOwners(),
                null,
                LocalDateTime.now()
            )

        fun randomCollectionElementWithParent() =
            Element.Collection(
                randomID(),
                randomName(),
                randomDescription(),
                randomOwners(),
                randomCollectionElement(),
                LocalDateTime.now()
            )

        fun randomTabElementWithParent() =
            Element.Tab(
                randomID(),
                randomName(),
                randomDescription(),
                randomLink(),
                randomOwners(),
                randomCollectionElement(),
                LocalDateTime.now()
            )

        fun randomFullName() = FullName.of(faker.name.name()).bind()
        fun randomEmail() = Email.of(faker.internet.email()).bind()
        fun randomPasswords() = Pair(
            Password.PlainPassword.of("Asdfghj12345!@#$%").bind(),
            Password.EncryptedPassword.of("Asdfghj12345!@#$%").bind()
        )

        fun randomUser() = User(
            randomID(),
            randomEmail(),
            randomFullName(),
            randomPasswords().second,
        )

        fun randomUserAndPlainPassword(): Pair<User, Password.PlainPassword> {
            val passwords = randomPasswords()

            return Pair(
                User(
                    randomID(),
                    randomEmail(),
                    randomFullName(),
                    passwords.second
                ),
                passwords.first
            )
        }

        fun randomJWT(
            user: User = ObjectMother.randomUser(),
            jwtConfig: JWTConfig = JWTConfig("example", "example", "example", "example"),
        ) =
            JWT.create()
                .withAudience(jwtConfig.audience)
                .withIssuer(jwtConfig.issuer)
                .withClaim("id", user.id.value)
                .withClaim("email", user.email.value)
                .withClaim("full_name", user.fullName.value)
                .withExpiresAt(Date(System.currentTimeMillis() + 60000))
                .sign(Algorithm.HMAC256(jwtConfig.secret))
    }
}
