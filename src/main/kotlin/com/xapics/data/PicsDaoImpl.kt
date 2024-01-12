package com.xapics.data

import com.xapics.data.DatabaseFactory.dbQuery
import com.xapics.data.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

class PicsDaoImpl : PicsDao {

    override suspend fun getPicsList(year: Int?, roll: String?, tag: String?, film: String?): List<Pic> {
        val query = (Films innerJoin Rolls innerJoin Pics).selectAll()
        year?.let { query.andWhere { Pics.year eq it } }
        roll?.let { query.andWhere { Rolls.title eq it } }
        tag?.let { query.andWhere { Pics.tags like "%$it%" } }
        film?.let { query.andWhere { Films.filmName eq it } }

        return dbQuery {
            query.map {
                Pic(
                    it[Pics.id].value,
                    it[Pics.year],
                    it[Pics.description],
                    it[Pics.imageUrl],
                    it[Pics.tags],
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

    override suspend fun editCollection(theUserId: Int, theCollection: String?, picId: Int?) {
        var isSuccess = false
        transaction {
            val thePic = PicEntity.findById(picId ?: 0)
            val coll = CollectionEntity.find {
                (Collections.title eq (theCollection ?: "") and
                        (Collections.title eq (theCollection ?: "") and
                                (Collections.pic eq picId)))
            }.firstOrNull()

            coll?.delete()
                ?: CollectionEntity.new {
                    userId = theUserId
                    title = theCollection ?: ""
                    pic = thePic!!
                }
            // isSuccess = true
        }
//        return isSuccess
    }

    override suspend fun getAllCollections(theUserId: Int): List<Pair<String, String>> {
        var query: List<ResultRow> = emptyList()
        transaction {
            query = (Collections innerJoin Pics)
                .select { Collections.userId eq theUserId }
                .distinctBy { it[Collections.title] }
        }

        return dbQuery {
            query.map {
                Pair(it[Collections.title], it[Pics.imageUrl])
            }
        }
    }

//    override suspend fun createCollection(theUserId: Int, theTitle: String, picId: Int) {
//        transaction {
//            CollectionEntity.new {
//                userId = theUserId
//                title = theTitle
//                pic = PicEntity.findById(picId)!!
//            }
//        }
//    }

    override suspend fun getCollection(userId: Int, collection: String): List<Pic> {
        val query = (Collections innerJoin Pics)
            .select { Collections.userId eq userId }
            .andWhere { Collections.title eq collection }

        return dbQuery {
            query.map {
                transaction {
                    val pic = PicEntity.findById(it[Collections.pic]) // TODO null-check
                    Pic(
                        pic!!.id.value,
                        pic.year,
                        pic.description,
                        pic.imageUrl,
                        pic.tags,
                        pic.roll.film.filmName,
                    )
                }
            }
        }
    }

    override suspend fun getPicCollections(userId: Int, picId: Int): List<String> {
        val query = (Collections innerJoin Pics)
            .select { Collections.userId eq userId }
            .andWhere { Pics.id eq picId }
        return dbQuery {
            query.map {
                it[Collections.title]
            }
        }
    }

}