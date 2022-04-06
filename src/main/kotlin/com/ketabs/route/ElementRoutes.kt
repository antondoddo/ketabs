package com.ketabs.route

import arrow.core.Either
import arrow.core.computations.ResultEffect.bind
import arrow.core.computations.either
import com.ketabs.model.Element
import com.ketabs.model.valueobject.ID
import com.ketabs.route.request.CreateElementRequest
import com.ketabs.route.request.CreateElementWithParentRequest
import com.ketabs.route.request.FindElementRequest
import com.ketabs.route.response.Response
import com.ketabs.route.response.withResponseHandler
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
        call.withResponseHandler {
            either {
                val req = Either.catch { call.receive<CreateElementRequest>() }.tapLeft {
                    when (it) {
                        is SerializationException -> Response.Failure { a -> a.respond(HttpStatusCode.BadRequest) }
                        else -> Response.Failure { a -> a.respond(HttpStatusCode.InternalServerError) }
                    }
                }.bind()

                val id = ID.of(call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString() ?: "").mapLeft {
                    Response.Failure { a -> a.respond(HttpStatusCode.Unauthorized) }
                }.bind()

                val data = req.parse(id).mapLeft {
                    Response.Failure { a -> a.respond(HttpStatusCode.UnprocessableEntity, it.toResponse()) }
                }.bind()

                val result = createElement(data).mapLeft {
                    when (it) {
                        is CreateElementError.WriteError -> Response.Failure { a -> a.respond(HttpStatusCode.InternalServerError) }
                    }
                }.bind()

                Response.Success { it.respond(HttpStatusCode.Created, result.toResponse()) }
            }
        }
    }

    post("/elements/{id}") {
        call.withResponseHandler {
            either {
                val req = Either.catch { call.receive<CreateElementWithParentRequest>() }.tapLeft {
                    when (it) {
                        is SerializationException -> Response.Failure { a -> a.respond(HttpStatusCode.BadRequest) }
                        else -> Response.Failure { a -> a.respond(HttpStatusCode.InternalServerError) }
                    }
                }.bind()

                req.parentID = Either.catch { call.parameters["id"]!! }.tapLeft {
                    Response.Failure { a -> a.respond(HttpStatusCode.NotFound) }
                }.bind()

                val id = ID.of(call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString() ?: "").mapLeft {
                    Response.Failure { a -> a.respond(HttpStatusCode.Unauthorized) }
                }.bind()

                val data = req.parse(id).mapLeft {
                    Response.Failure { call -> call.respond(HttpStatusCode.UnprocessableEntity, it.toResponse()) }
                }.bind()

                val result = createElementWithParent(data).mapLeft {
                    when (it) {
                        is CreateElementWithParentError.InvalidOwner -> Response.Failure { a -> a.respond(HttpStatusCode.Unauthorized) }
                        is CreateElementWithParentError.WriteError -> Response.Failure { a -> a.respond(HttpStatusCode.InternalServerError) }
                        is CreateElementWithParentError.ReadError -> Response.Failure { a -> a.respond(HttpStatusCode.InternalServerError) }
                        is CreateElementWithParentError.ElementNotFound -> Response.Failure { a -> a.respond(HttpStatusCode.NotFound) }
                        is CreateElementWithParentError.InvalidParentElement -> Response.Failure { a ->
                            a.respond(
                                HttpStatusCode.UnprocessableEntity,
                                mapOf("id" to it.message),
                            )
                        }
                    }
                }.bind()

                Response.Success { it.respond(HttpStatusCode.Created, result.toResponse()) }
            }
        }
    }

    get("/elements/{id}") {
        call.withResponseHandler {
            either {
                val id = Either.catch { call.parameters["id"]!! }.tapLeft {
                    Response.Failure { a -> a.respond(HttpStatusCode.NotFound) }
                }.bind()

                val data = FindElementRequest(id).parse().mapLeft {
                    Response.Failure { call -> call.respond(HttpStatusCode.NotFound, it.toResponse()) }
                }.bind()

                val result = findElement(data).mapLeft {
                    when (it) {
                        is FindElementError.ElementNotFound -> Response.Failure { a -> a.respond(HttpStatusCode.NotFound) }
                        is FindElementError.ReadError -> Response.Failure { a -> a.respond(HttpStatusCode.InternalServerError) }
                    }
                }.bind()

                Response.Success { it.respond(HttpStatusCode.OK, result.toResponse()) }
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
