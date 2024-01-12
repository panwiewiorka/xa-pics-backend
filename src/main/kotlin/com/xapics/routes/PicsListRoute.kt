package com.xapics.routes

import com.xapics.data.PicsDao
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.picsList(
    picsDao: PicsDao
) {
    get("picslist") {
        val year = call.request.queryParameters["year"]?.toInt()
        val roll = call.request.queryParameters["roll"]
        val tag = call.request.queryParameters["tag"]
        val film = call.request.queryParameters["film"]

        if (year == null && roll.isNullOrBlank() && tag.isNullOrBlank() && film.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest
//                HttpStatusCode.OK,
//                listOf(FilmPic(0, 0, "NULL", "$BASE_URL/pics/null.jpg", "", ""))
            )
        } else {
            call.respond(
                HttpStatusCode.OK,
                picsDao.getPicsList(year, roll, tag, film)
            )
        }
    }
}