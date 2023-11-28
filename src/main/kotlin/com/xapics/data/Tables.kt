package com.xapics.data

import com.xapics.data.models.FilmType
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Films : IntIdTable() {
    val name = varchar("name", 255)
    val iso = integer("iso")
    val type = enumeration<FilmType>("type")
    val xpro = bool("xpro")
    val expired = bool("expired")
}

class FilmEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FilmEntity>(Films)
    var name by Films.name
    var iso by Films.iso
    var type by Films.type
    var xpro by Films.xpro
    var expired by Films.expired
}


object Rolls : IntIdTable() {
    val title = varchar("title", 255)
    val film = varchar("film", 255)
    val path = varchar("path", 255)
}

class RollEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RollEntity>(Rolls)
    var title by Rolls.title
    var film by Rolls.film
    var path by Rolls.path
    val frames by PicEntity referrersOn Pics.roll
}


object Pics : IntIdTable() {
    val year = integer("year")
    val description = varchar("description", 255)
    val imageUrl = varchar("imageUrl", 255)
    val roll = reference("roll", Rolls)
}

class PicEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PicEntity>(Pics)
    var year by Pics.year
    var description by Pics.description
    var imageUrl by Pics.imageUrl
    var roll by RollEntity referencedOn Pics.roll
}