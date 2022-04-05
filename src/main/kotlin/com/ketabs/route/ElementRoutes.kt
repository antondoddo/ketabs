package com.ketabs.route

import arrow.core.Either
import com.ketabs.model.Element
import com.ketabs.model.valueobject.ID
import com.ketabs.route.request.CreateElementRequest
import com.ketabs.route.request.CreateElementWithParentRequest
import com.ketabs.route.request.FindElementRequest
import com.ketabs.service.CreateElement
import com.ketabs.service.CreateElementError
import com.ketabs.service.CreateElementWithParent
import com.ketabs.service.CreateElementWithParentError
import com.ketabs.service.FindElement
import com.ketabs.service.FindElementError
import io.ktor.application.call
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.principal
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import kotlinx.serialization.SerializationException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun Route.elements(
    findElement: FindElement,
    createElement: CreateElement,
    createElementWithParent: CreateElementWithParent,
) {
    post("/elements") {
        let {
            when (val request = Either.catch { call.receive<CreateElementRequest>() }) {
                is Either.Right -> request.value
                is Either.Left -> when (request.value) {
                    is SerializationException -> return@post call.response.status(HttpStatusCode.BadRequest)
                    else -> return@post call.response.status(HttpStatusCode.InternalServerError)
                }
            }
        }.let {
            val principal = call.principal<JWTPrincipal>()
            when (val userID = ID.of(principal?.payload?.getClaim("id")?.asString() ?: "")) {
                is Either.Left -> return@post call.response.status(HttpStatusCode.Unauthorized)
                is Either.Right -> Pair(it, userID.value)
            }
        }.let {
            when (val parsed = it.first.parse(it.second)) {
                is Either.Right -> parsed.value
                is Either.Left -> return@post call.respond(
                    HttpStatusCode.UnprocessableEntity,
                    parsed.value.toResponse()
                )
            }
        }.let {
            when (val result = createElement(it)) {
                is Either.Right -> return@post call.respond(HttpStatusCode.Created, result.value.toResponse())
                is Either.Left -> result.value
            }
        }.let {
            when (it) {
                is CreateElementError.WriteError -> return@post call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }

    post("/elements/{id}") {
        let {
            when (val request = Either.catch { call.receive<CreateElementWithParentRequest>() }) {
                is Either.Right -> request.value
                is Either.Left -> when (request.value) {
                    is SerializationException -> return@post call.response.status(HttpStatusCode.BadRequest)
                    else -> return@post call.response.status(HttpStatusCode.InternalServerError)
                }
            }
        }.let {
            when (val id = call.parameters["id"]) {
                is String -> {
                    it.parentID = id
                    it
                }
                else -> return@post call.respond(HttpStatusCode.NotFound)
            }
        }.let {
            val principal = call.principal<JWTPrincipal>()
            when (val userID = ID.of(principal?.payload?.getClaim("id")?.asString() ?: "")) {
                is Either.Left -> return@post call.response.status(HttpStatusCode.Unauthorized)
                is Either.Right -> Pair(it, userID.value)
            }
        }.let {
            when (val parsed = it.first.parse(it.second)) {
                is Either.Right -> parsed.value
                is Either.Left -> when (parsed.value.containsKey("id")) {
                    false -> return@post call.respond(HttpStatusCode.UnprocessableEntity, parsed.value.toResponse())
                    true -> return@post call.respond(HttpStatusCode.NotFound)
                }
            }
        }.let {
            when (val result = createElementWithParent(it)) {
                is Either.Right -> return@post call.respond(HttpStatusCode.Created, result.value.toResponse())
                is Either.Left -> result.value
            }
        }.let {
            when (it) {
                is CreateElementWithParentError.InvalidOwner -> return@post call.respond(HttpStatusCode.Unauthorized)
                is CreateElementWithParentError.WriteError -> return@post call.respond(HttpStatusCode.InternalServerError)
                is CreateElementWithParentError.ReadError -> return@post call.respond(HttpStatusCode.InternalServerError)
                is CreateElementWithParentError.ElementNotFound -> return@post call.respond(HttpStatusCode.NotFound)
                is CreateElementWithParentError.InvalidParentElement -> return@post call.respond(
                    HttpStatusCode.UnprocessableEntity, mapOf("id" to it.message)
                )
            }
        }
    }

    get("/elements/{id}") {
        let {
            when (val id = call.parameters["id"]) {
                is String -> id
                else -> return@get call.respond(HttpStatusCode.NotFound)
            }
        }.let {
            when (val parsed = FindElementRequest(it).parse()) {
                is Either.Right -> parsed.value
                is Either.Left -> return@get call.respond(HttpStatusCode.NotFound)
            }
        }.let {
            when (val result = findElement(it)) {
                is Either.Right -> return@get call.respond(HttpStatusCode.OK, result.value.toResponse())
                is Either.Left -> result.value
            }
        }.let {
            when (it) {
                is FindElementError.ElementNotFound -> return@get call.respond(HttpStatusCode.NotFound)
                is FindElementError.ReadError -> return@get call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}

private fun <K, V> Map<K, V>.toResponse() = mapOf("errors" to this)

private fun Element.toResponse(): Map<String, String?> {
    val data = mutableMapOf(
        "id" to this.id.value,
        "name" to this.name.value,
        "description" to this.description.value,
        "parent_id" to this.parent.let {
            when (it == null) {
                true -> null
                false -> it.id.value
            }
        },
        "createdAt" to this.createdAt.toResponse(),
        "updatedAt" to this.updatedAt?.toResponse(),
        "trashedAt" to this.trashedAt?.toResponse(),
    )

    return when (this) {
        is Element.Collection -> data
        is Element.Tab -> {
            data["link"] = this.link.toString()
            data
        }
    }
}

private fun LocalDateTime.toResponse() = this.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
