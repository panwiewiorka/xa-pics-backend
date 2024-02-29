package com.xapics.data

import com.xapics.data.models.FilmType
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        val jdbcURL = "jdbc:sqlite:./db_pics"
        val driverClassName = "org.sqlite.JDBC"

//        val database = Database.connect(jdbcURL, driverClassName)
        Database.connect(jdbcURL, driverClassName)

        transaction {
            SchemaUtils.create(Users, Films, Rolls, Pics, Collections)
//            SchemaUtils.createMissingTablesAndColumns(Users)

            // print sql to std-out
            addLogger(StdOutSqlLogger)

            FilmEntity.all().forEach{ it.delete() }
            PicEntity.all().forEach{ it.delete() }
            RollEntity.all().forEach{ it.delete() }
            UserEntity.all().forEach{ it.delete() }
            CollectionEntity.all().forEach{ it.delete() }

            val aeroFilm = FilmEntity.new {
                filmName = "Aerocolor"
                iso = 125
                type = FilmType.NEGATIVE
            }
            val ektaFilm = FilmEntity.new {
                filmName = "Ektachrome"
                iso = 100
                type = FilmType.SLIDE
            }

            val ektaRoll = RollEntity.new {
                index = 1
                title = "Ekta"
                path = "/rolls/01-Ektachrome-Ekta"
                film = ektaFilm
                xpro = false
                expired = false
                nonXa = false
            }
            val aeroRoll = RollEntity.new {
                index = 2
                title = "Aero"
                path = "/rolls/02-Aerocolor-Aero"
                film = aeroFilm
                xpro = false
                expired = false
                nonXa = false
            }

            PicEntity.new {
                year = 2023
                description = "House gate"
                imageUrl = "/pics/09ektachrome/01.jpg"
                hashtags = "Caucasus"
                roll = ektaRoll
            }
            PicEntity.new {
                year = 2020
                description = "Bench, house"
                imageUrl = "/pics/09ektachrome/02.jpg"
                hashtags = "Caucasus"
                roll = ektaRoll
            }
            PicEntity.new {
                year = 2020
                description = "Girls"
                imageUrl = "/pics/09ektachrome/03.jpg"
                hashtags = "Caucasus, people"
                roll = ektaRoll
            }
            PicEntity.new {
                year = 2020
                description = "Junk"
                imageUrl = "/pics/09ektachrome/04.jpg"
                hashtags = "Caucasus"
                roll = ektaRoll
            }
            PicEntity.new {
                year = 2021
                description = "Curtain"
                imageUrl = "/pics/09ektachrome/05.jpg"
                hashtags = "Caucasus"
                roll = ektaRoll
            }

            PicEntity.new {
                year = 2020
                description = "Sunset river, city"
                imageUrl = "/pics/01aerocolor/01.jpg"
                hashtags = "Poland, city"
                roll = aeroRoll
            }
            PicEntity.new {
                year = 2023
                description = "Man on a wheelchair"
                imageUrl = "/pics/01aerocolor/02.jpg"
                hashtags = "Poland, people, night"
                roll = aeroRoll
            }
            PicEntity.new {
                year = 2023
                description = "People in lecture room"
                imageUrl = "/pics/01aerocolor/03.jpg"
                hashtags = "Poland, people"
                roll = aeroRoll
            }
            PicEntity.new {
                year = 2023
                description = "Music concert in lecture room"
                imageUrl = "/pics/01aerocolor/04.jpg"
                hashtags = "Poland, people"
                roll = aeroRoll
            }
            PicEntity.new {
                year = 2021
                description = "Self-portrait"
                imageUrl = "/pics/01aerocolor/05.jpg"
                hashtags = "Poland, people, self-portrait"
                roll = aeroRoll
            }
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}