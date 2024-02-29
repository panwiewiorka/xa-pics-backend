package com.xapics.routes

import com.xapics.data.PicsDao
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.search(
    picsDao: PicsDao,
) {
    get("search") {
        val searchQuery = call.request.queryParameters["query"]

        if (searchQuery.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest
            )
        } else {
            call.respond(
                HttpStatusCode.OK,
                picsDao.getSearchResponse(searchQuery)
            )
        }
    }
}