package com.xapics.data

import com.xapics.data.DatabaseFactory.dbQuery
import com.xapics.data.models.Film
import com.xapics.data.models.FilmPic
import org.jetbrains.exposed.sql.*

class PicsDaoImpl : PicsDao {
    override suspend fun getFilmPicsList(year: Int?, film: String?): List<FilmPic> {
//        return listOf(FilmPic(0, "NULL", "$BASE_URL/pics/null.jpg", ""))
        val query = (Rolls innerJoin Pics).selectAll()
        year?.let { query.andWhere { Pics.year eq it } }
        film?.let { query.andWhere { Rolls.film eq it } }

        return dbQuery {
            query.map {
                FilmPic(
                    it[Pics.year],
                    it[Pics.description],
                    it[Pics.imageUrl],
                    it[Rolls.film]
                )
            }
        }
    }

    override suspend fun getFilmsList(): List<Film> {
        val query = Films.selectAll()
        return dbQuery {
            query.map {
                Film(
                    it[Films.name],
                    it[Films.iso].toInt(),
                    it[Films.type],
                    it[Films.xpro],
                    it[Films.expired],
                )
            }
        }
    }
}