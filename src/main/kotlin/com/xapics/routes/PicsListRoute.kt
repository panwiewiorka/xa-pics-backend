package com.xapics.routes

import com.xapics.data.PicsDao
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.picsList(
    picsDao: PicsDao,
    baseUrl: String
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
                picsDao.getPicsList(query, baseUrl)
            )
        }

        /*
val year = call.request.queryParameters["year"]?.toInt()
val roll = call.request.queryParameters["roll"]
val tag = call.request.queryParameters["tag"]
val film = call.request.queryParameters["film"]
val description = call.request.queryParameters["description"]

if (year == null && roll.isNullOrBlank() && tag.isNullOrBlank() && film.isNullOrBlank() && description.isNullOrBlank()) {
    call.respond(
        HttpStatusCode.BadRequest
//                HttpStatusCode.OK,
//                listOf(FilmPic(0, 0, "NULL", "$BASE_URL/pics/null.jpg", "", ""))
    )
} else {
    call.respond(
        HttpStatusCode.OK,
        picsDao.getPicsList(year, roll, tag, film, description, baseUrl)
    )
}
 */
    }
}