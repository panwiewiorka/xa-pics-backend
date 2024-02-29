package com.xapics.routes

import com.xapics.data.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

fun Route.rolls(
    picsDao: PicsDao,
) {
    get("rollthumbs") {
        call.respond(
            HttpStatusCode.OK,
            picsDao.getRollThumbs()
        )
    }

    get("rolls") {
        call.respond(
            HttpStatusCode.OK,
            picsDao.getRollsList()
        )
    }

    val log = LoggerFactory.getLogger(this.javaClass)

    post("rolls") {
        val roll = call.receiveParameters()
//        log.debug("roll title = ${roll["title"]}")
//        log.debug("roll film = ${roll["film"]}")
        transaction {
            val filmEntity = FilmEntity.find { Films.filmName eq (roll["film"] ?: "nullFilm") }.first() // TODO firstOrNull ?
            val rollTitle = roll["title"] ?: "nullRoll" // TODO filter spaces and other special symbols (FOR PATH ONLY)

            if (roll["isNewRoll"] == "true") {
                val rollIndex = (Rolls.selectAll().count() + 1).toInt()
                val stringIndex = String.format("%02d", rollIndex)

                RollEntity.new {
                    index = rollIndex
                    title = rollTitle
                    path = "/rolls/$stringIndex-${filmEntity.filmName}-$rollTitle"
                    film = filmEntity
                    nonXa = roll["nonXa"] == "true"
                }
            } else {
                val rollToEdit = RollEntity.find { Rolls.title eq rollTitle }.first()
                rollToEdit.path = "/rolls/${rollToEdit.index}-${filmEntity.filmName}-$rollTitle"
                rollToEdit.film = filmEntity
                rollToEdit.nonXa = roll["nonXa"] == "true"
            }
        }
        call.respond(HttpStatusCode.OK)
    }
}