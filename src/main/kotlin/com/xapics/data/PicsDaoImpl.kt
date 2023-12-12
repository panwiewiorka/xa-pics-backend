package com.xapics.data

import com.xapics.data.DatabaseFactory.dbQuery
import com.xapics.data.models.Film
import com.xapics.data.models.FilmPic
import com.xapics.data.models.Roll
import org.jetbrains.exposed.sql.*

class PicsDaoImpl : PicsDao {
    override suspend fun getFilmPicsList(year: Int?, roll: String?, film: String?): List<FilmPic> {
        val query = (Films innerJoin Rolls innerJoin Pics).selectAll()
        year?.let { query.andWhere { Pics.year eq it } }
        roll?.let { query.andWhere { Rolls.title eq it } }
        film?.let { query.andWhere { Films.filmName eq it } }

        return dbQuery {
            query.map {
                FilmPic(
                    it[Pics.year],
                    it[Pics.description],
                    it[Pics.imageUrl],
                    it[Films.filmName]
                )
            }
        }
    }

    override suspend fun getFilmsList(): List<Film> {
        val query = Films.selectAll()
        return dbQuery {
            query.map {
                Film(
                    it[Films.filmName],
                    it[Films.iso].toInt(),
                    it[Films.type],
                    it[Films.xpro],
                    it[Films.expired],
                )
            }
        }
    }

    override suspend fun getRollsList(): List<Roll> {
        val query = (Films innerJoin Rolls).selectAll()
        return dbQuery {
            query.map {
                Roll(
                    it[Rolls.title],
                    it[Films.filmName],
                    it[Rolls.nonxa]
                )
            }
        }
    }

    override suspend fun getRollThumbs(): List<Pair<String, String>> {
        val query = Rolls.selectAll()
        return dbQuery {
            query.toList().map {
                val rollNumber = it[Rolls.id].value
                Pair(
                    RollEntity[rollNumber].frames.toList().random().imageUrl,
                    RollEntity[rollNumber].title
                )
            }
        }
    }
}