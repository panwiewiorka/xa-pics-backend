package com.xapics.routes

import com.xapics.data.PicsDao
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.tags(
    picsDao: PicsDao,
) {
    get("tags") {
        call.respond(
            HttpStatusCode.OK,
            picsDao.getAllTags()
        )
    }
}