package com.xapics.routes

import com.xapics.data.PicsDao
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.randomPic(
    picsDao: PicsDao,
) {
    get("randompic") {
        call.respond(
            HttpStatusCode.OK,
            picsDao.getRandomPic()
        )
    }
}