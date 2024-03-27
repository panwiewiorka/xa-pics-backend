package com.xapics.data

import com.xapics.data.models.FilmType
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Users: IntIdTable() {
    val username = varchar("username", 50)
    val password =  varchar("password", 255)
    val salt = varchar("salt", 255)
}

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(Users)
    var username by Users.username
    var password by Users.password
    var salt by Users.salt
}


object Collections: IntIdTable() {
    val userId = integer("userId")
    val title = varchar("collection", 255)
    val pic = reference("pic", Pics)
}

class CollectionEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CollectionEntity>(Collections)
    var userId by Collections.userId
    var title by Collections.title
    var pic by PicEntity referencedOn Collections.pic
}


object Films : IntIdTable() {
    val filmName = varchar("filmName", 255)
    val iso = integer("iso")
    val type = enumeration<FilmType>("type")
}

class FilmEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FilmEntity>(Films)
    var filmName by Films.filmName
    var iso by Films.iso
    var type by Films.type
    val rolls by RollEntity referrersOn Rolls.film
}


object Rolls : IntIdTable() {
    val index = integer("index").uniqueIndex()
    val title = varchar("title", 255)
//    val path = varchar("path", 255)
    val film = reference("film", Films)
    val xpro = bool("xpro")
    val expired = bool("expired")
//    val nonxa = bool("nonXa")
    val dateCreated = datetime("date_created").defaultExpression(CurrentDateTime)
}

class RollEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RollEntity>(Rolls)
    var index by Rolls.index
    var title by Rolls.title
//    var path by Rolls.path
    var film by FilmEntity referencedOn Rolls.film
    var xpro by Rolls.xpro
    var expired by Rolls.expired
//    var nonXa by Rolls.nonxa
    val frames by PicEntity referrersOn Pics.roll
    var dateCreated by Rolls.dateCreated
}


object Pics : IntIdTable() {
    val year = integer("year")
    val description = varchar("description", 255)
    val keywords = varchar("keywords", 255)
    val imageUrl = varchar("imageUrl", 255)
    val hashtags = varchar("hashtags", 255)
    val roll = reference("roll", Rolls)
    val dateCreated = datetime("date_created").defaultExpression(CurrentDateTime)
}

class PicEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PicEntity>(Pics)
    var year by Pics.year
    var description by Pics.description
    var keywords by Pics.keywords
    var imageUrl by Pics.imageUrl
    var hashtags by Pics.hashtags
    var roll by RollEntity referencedOn Pics.roll
    var dateCreated by Pics.dateCreated
}