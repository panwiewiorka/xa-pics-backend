package com.xapics.routes

import com.xapics.data.PicsDao
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.picsList(
    picsDao: PicsDao,
) {
    get("picslist") {
        val query = call.request.queryParameters["query"]

        if (query.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest
            )
        } else {
            call.respond(
                HttpStatusCode.OK,
                picsDao.getPicsList(query)
            )
        }
    }
}