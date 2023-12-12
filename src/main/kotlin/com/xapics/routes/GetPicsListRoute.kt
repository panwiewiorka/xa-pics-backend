package com.xapics.routes

import com.xapics.data.PicsDao
import com.xapics.data.models.BASE_URL
import com.xapics.data.models.FilmPic
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.getPicsList(
    picsDaoImpl: PicsDao
) {
    get("picslist") {
        val year = call.request.queryParameters["year"]?.toInt()
        val roll = call.request.queryParameters["roll"]
        val film = call.request.queryParameters["film"]

        if (year == null && roll.isNullOrBlank() && film.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.OK,
                listOf(FilmPic(0, "NULL", "$BASE_URL/pics/null.jpg", ""))
            )
        } else {
            call.respond(
                HttpStatusCode.OK,
                picsDaoImpl.getFilmPicsList(year, roll, film)
            )
        }
    }
}

/**
private val ekta = listOf(
Pic(2020,"House gate", "$BASE_URL/pics/09ektachrome/01.jpg"),
Pic(2020,"Bench, house", "$BASE_URL/pics/09ektachrome/02.jpg"),
Pic(2020,"Girls", "$BASE_URL/pics/09ektachrome/03.jpg"),
Pic(2020,"Junk", "$BASE_URL/pics/09ektachrome/04.jpg"),
Pic(2020,"Curtain", "$BASE_URL/pics/09ektachrome/05.jpg"),
Pic(2020,"Weird house", "$BASE_URL/pics/09ektachrome/06.jpg"),
Pic(2020,"Narts path", "$BASE_URL/pics/09ektachrome/07.jpg"),
Pic(2020,"Sheep", "$BASE_URL/pics/09ektachrome/08.jpg"),
Pic(2020,"Cut tree", "$BASE_URL/pics/09ektachrome/09.jpg"),
Pic(2021,"Blue house", "$BASE_URL/pics/09ektachrome/10.jpg"),
)
val ektaFilm = FilmRoll(ekta, "Ektachrome")

private val aero = listOf(
Pic(2020,"Sunset river, city", "$BASE_URL/pics/01aerocolor/01.jpg"),
Pic(2023,"Man on a wheelchair, night, people", "$BASE_URL/pics/01aerocolor/02.jpg"),
Pic(2023,"People in lecture room", "$BASE_URL/pics/01aerocolor/03.jpg"),
Pic(2023,"People in lecture room, concert, music", "$BASE_URL/pics/01aerocolor/04.jpg"),
Pic(2021,"Self portrait", "$BASE_URL/pics/01aerocolor/05.jpg"),
)
val aeroFilm = FilmRoll(aero, "Aerocolor")

private val picsList = listOf(ektaFilm, aeroFilm)

fun Route.listOfPics() {
    get("/picslistttttttttttttttttttttttttt") {
        val year: String? = call.request.queryParameters["year"]
        val film: String? = call.request.queryParameters["film"]

        if ((year.isNullOrBlank()) && (film.isNullOrBlank())) {
            call.respond(
                HttpStatusCode.OK,
                listOf(FilmPic(0, "NULL", "$BASE_URL/pics/null.jpg", ""))
            )
        } else {

            /**
            call.respond(HttpStatusCode.OK) {
            val emptyRoll = listOf(FilmRoll(listOf(Pic(0, "EMPTY", "$BASE_URL/pics/empty.jpg")), ""))

            val filmRolls = if(film != null) {
            picsList.filter { it.film == film }
            .ifEmpty { emptyRoll }
            } else picsList

            filmRolls.flatMap { filmRoll ->
            List(filmRoll.pics.size) {
            FilmPic(
            filmRoll.pics[it].year,
            filmRoll.pics[it].description,
            filmRoll.pics[it].imageUrl,
            filmRoll.film
            )
            }
            }
            .filter { if (year == null) true else it.year.toString() == year } // TODO get rid of if(true):iterating thru all elements, replace by: if(!null) -> filter
            .ifEmpty { listOf(FilmPic(0, "EMPTY", "$BASE_URL/pics/empty.jpg", "")) }
            }
             */

            call.respond(
                HttpStatusCode.OK,
                picsList
                    .filter { if (film == null) true else it.film == film }
                    .flatMap { filmRoll ->
                    List(filmRoll.pics.size) {
                        FilmPic(
                            filmRoll.pics[it].year,
                            filmRoll.pics[it].description,
                            filmRoll.pics[it].imageUrl,
                            filmRoll.film
                        )
                    }
                }
                    .filter { if (year == null) true else it.year.toString() == year } // TODO get rid of if(true):iterating thru all elements, replace by: if(!null) -> filter
                    .ifEmpty { listOf(FilmPic(0, "EMPTY", "$BASE_URL/pics/empty.jpg", "")) }
            )
        }
    }
}


fun Route.randomPic() {
    get("/randompic") {
        call.respond(
            HttpStatusCode.OK,
            picsList.flatMap { filmRoll ->
                List(filmRoll.pics.size) {
                    FilmPic(
                        filmRoll.pics[it].year,
                        filmRoll.pics[it].description,
                        filmRoll.pics[it].imageUrl,
                        filmRoll.film
                    )
                }
            }.random() // TODO more effective (w/o flattening everything. Pick random film, remember film attributes, pick random Pic and map to FilmPic)
        )
    }
}
 */