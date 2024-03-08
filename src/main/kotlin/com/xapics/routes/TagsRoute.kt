package com.xapics.routes

import com.xapics.data.PicsDao
import com.xapics.data.models.TheString
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.tags(
    picsDao: PicsDao,
) {
    get("v1/tags") {
        val message = picsDao.getAllTags()
        call.respond(
            status = HttpStatusCode.OK,
            message = TheString(message)
        )
    }

    get("v1/filteredtags") {
        val query = call.request.queryParameters["query"]

        val message = picsDao.getFilteredTags(query ?: "")
            call.respond(
                status = HttpStatusCode.OK,
                message = TheString(message)
            )
    }
}