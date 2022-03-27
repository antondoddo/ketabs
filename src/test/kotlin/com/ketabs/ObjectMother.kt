package com.ketabs

import arrow.core.computations.ResultEffect.bind
import com.ketabs.model.Element
import com.ketabs.model.valueobject.Description
import com.ketabs.model.valueobject.ID
import com.ketabs.model.valueobject.Link
import com.ketabs.model.valueobject.Name
import io.github.serpro69.kfaker.faker
import java.time.LocalDateTime

class ObjectMother {

    companion object {
        private val faker = faker { }

        fun randomName() = Name.of(faker.app.name()).bind()
        fun randomDescription() = Description.of(faker.book.title()).bind()
        fun randomLink() = Link.of(faker.siliconValley.urls()).bind()

        fun randomCollectionElement() =
            Element.Collection(ID.random(), randomName(), randomDescription(), null, LocalDateTime.now())

        fun randomTabElement() =
            Element.Tab(ID.random(), randomName(), randomDescription(), randomLink(), null, LocalDateTime.now())

        fun randomCollectionElementWithParent() =
            Element.Collection(
                ID.random(),
                randomName(),
                randomDescription(),
                randomCollectionElement(),
                LocalDateTime.now()
            )

        fun randomTabElementWithParent() =
            Element.Tab(
                ID.random(),
                randomName(),
                randomDescription(),
                randomLink(),
                randomCollectionElement(),
                LocalDateTime.now()
            )
    }
}
