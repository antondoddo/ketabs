package com.ketabs.route.response

import arrow.core.Either
import io.ktor.application.ApplicationCall

typealias respond = suspend (ApplicationCall) -> Unit

sealed class Response {
    data class Success(val action: respond) : Response()
    data class Failure(val action: respond) : Response()
}

suspend inline fun ApplicationCall.withResponseHandler(action: () -> Either<Response.Failure, Response.Success>) {
    action.invoke().tapLeft { it.action(this) }.tap { it.action(this) }
}
