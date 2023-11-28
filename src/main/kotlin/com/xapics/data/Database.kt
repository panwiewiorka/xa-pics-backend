package com.xapics.data

import com.xapics.data.models.FilmType
import com.xapics.routes.BASE_URL
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
            SchemaUtils.create(Films, Rolls, Pics)
//            SchemaUtils.createMissingTablesAndColumns(Users)

            // print sql to std-out
            addLogger(StdOutSqlLogger)

            FilmEntity.all().forEach{ it.delete() }
            PicEntity.all().forEach{ it.delete() }
            RollEntity.all().forEach{ it.delete() }

            FilmEntity.new {
                name = "Aerocolor"
                iso = 125
                type = FilmType.NEGATIVE
                xpro = false
                expired = false
            }
            FilmEntity.new {
                name = "Ektachrome"
                iso = 100
                type = FilmType.SLIDE
                xpro = false
                expired = false
            }

            val ekta = RollEntity.new {
                title = "Ekta"
                film = "Ektachrome"
                path = "$BASE_URL/pics/09ektachrome"
            }
            val aero = RollEntity.new {
                title = "Aero"
                film = "Aerocolor"
                path = "$BASE_URL/pics/01aerocolor"
            }

            PicEntity.new {
                year = 2023
                description = "House gate"
                imageUrl = "$BASE_URL/pics/09ektachrome/01.jpg"
                roll = ekta
            }
            PicEntity.new {
                year = 2020
                description = "Bench, house"
                imageUrl = "$BASE_URL/pics/09ektachrome/02.jpg"
                roll = ekta
            }
            PicEntity.new {
                year = 2020
                description = "Girls"
                imageUrl = "$BASE_URL/pics/09ektachrome/03.jpg"
                roll = ekta
            }
            PicEntity.new {
                year = 2020
                description = "Junk"
                imageUrl = "$BASE_URL/pics/09ektachrome/04.jpg"
                roll = ekta
            }
            PicEntity.new {
                year = 2021
                description = "Curtain"
                imageUrl = "$BASE_URL/pics/09ektachrome/05.jpg"
                roll = ekta
            }

            PicEntity.new {
                year = 2020
                description = "Sunset river, city"
                imageUrl = "$BASE_URL/pics/01aerocolor/01.jpg"
                roll = aero
            }
            PicEntity.new {
                year = 2023
                description = "Man on a wheelchair, night, people"
                imageUrl = "$BASE_URL/pics/01aerocolor/02.jpg"
                roll = aero
            }
            PicEntity.new {
                year = 2023
                description = "People in lecture room"
                imageUrl = "$BASE_URL/pics/01aerocolor/03.jpg"
                roll = aero
            }
            PicEntity.new {
                year = 2023
                description = "People in lecture room, concert, music"
                imageUrl = "$BASE_URL/pics/01aerocolor/04.jpg"
                roll = aero
            }
            PicEntity.new {
                year = 2021
                description = "Self portrait"
                imageUrl = "$BASE_URL/pics/01aerocolor/05.jpg"
                roll = aero
            }
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}