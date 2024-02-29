package com.xapics.data

import com.xapics.data.DatabaseFactory.dbQuery
import com.xapics.data.models.*
import com.xapics.data.models.FilmType.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

class PicsDaoImpl : PicsDao {

    override suspend fun getPicsList(theQuery: String, baseUrl: String): List<Pic> {
        val tags = theQuery
            .split(", ")
            .map { it.split(" = ") }
            .map { Tag(it[0], it[1]) }

        val query = (Films innerJoin Rolls innerJoin Pics).selectAll()
        tags.forEach { tag ->
            when(tag.type) {
                "filmType" -> {
                    val type = when(tag.value) {
                        "SLIDE" -> SLIDE
                        "NEGATIVE" -> NEGATIVE
                        "BW" -> BW
                        else -> NULL
                    }
                    query.andWhere { Films.type eq type }
                }
                "roll" -> { query.andWhere { Rolls.title eq tag.value } }
                "nonXa" -> { query.andWhere { Rolls.nonxa eq (tag.value == "true") } }
                "expired" -> { query.andWhere { Rolls.expired eq (tag.value == "true") } }
                "xpro" -> { query.andWhere { Rolls.xpro eq (tag.value == "true") } }
                "iso" -> { query.andWhere { Films.iso eq tag.value.toInt() } }
                "filmName" -> { query.andWhere { Films.filmName eq tag.value } }
                "year" -> { query.andWhere { Pics.year eq tag.value.toInt() } }
                "hashtag" -> { query.andWhere { Pics.hashtags like "%${tag.value}%" } }
                "search" -> { query.andWhere { Pics.description like "%${tag.value}%" } } // TODO search all? here?
            }
        }

        return dbQuery {
            query.map { // TODO fun toPic()     (to avoid copyPaste vvvvv)
                Pic(
                    it[Pics.id].value,
                    baseUrl + it[Pics.imageUrl],
                    it[Pics.description],
                    listOf(
                        "year = ${it[Pics.year]}",
                        "filmName = ${it[Films.filmName]}",
                        "filmType = ${it[Films.type]}",
                        "iso = ${it[Films.iso]}",
                        "expired = ${it[Rolls.expired]}",
                        "xpro = ${it[Rolls.xpro]}",
                        "nonXa = ${it[Rolls.nonxa]}",
                        it[Pics.hashtags].toString().split(',').map { t-> "hashtag = ${t.trim()}" }.sorted().toString().drop(1).dropLast(1)
                    ).toString().drop(1).dropLast(1)
                )
            }
        }
    }
    /*
override suspend fun getPicsList(year: Int?, roll: String?, tag: String?, film: String?, description: String?, baseUrl: String): List<Pic> {
        val query = (Films innerJoin Rolls innerJoin Pics).selectAll()
        year?.let { query.andWhere { Pics.year eq it } }
        roll?.let { query.andWhere { Rolls.title eq it } }
        tag?.let { query.andWhere { Pics.tags like "%$it%" } }
        film?.let { query.andWhere { Films.filmName eq it } }
        description?.let { query.andWhere { Pics.description like "%$it%" } }

        return dbQuery {
            query.map {
                Pic(
                    it[Pics.id].value,
                    it[Pics.year],
                    it[Pics.description],
                    baseUrl + it[Pics.imageUrl],
                    it[Pics.tags],
                    it[Films.filmName],
                    it[Films.type],
                    it[Films.iso],
                    it[Rolls.expired],
                    it[Rolls.xpro],
                    it[Rolls.nonxa]
                )
            }
        }
    }

     */

    override suspend fun getSearchResponse(searchQuery: String, baseUrl: String): List<Pic> {
        val qq = searchQuery.replace(',', ' ').trim().split("\\s+".toRegex())
        val query = (Films innerJoin Rolls innerJoin Pics).selectAll()
        qq.forEach {
            query
                .orWhere { Pics.description like "%$it%" }
                .orWhere { Pics.hashtags like "%$it%" }
                .orWhere { Rolls.title like "%$it%" }
                .orWhere { Films.filmName like "%$it%" }
        }

        return dbQuery {
            query.distinct().map {
                Pic(
                    it[Pics.id].value,
                    baseUrl + it[Pics.imageUrl],
                    it[Pics.description],
                    listOf(
                        "year = ${it[Pics.year]}",
                        "filmName = ${it[Films.filmName]}",
                        "filmType = ${it[Films.type]}",
                        "iso = ${it[Films.iso]}",
                        "expired = ${it[Rolls.expired]}",
                        "xpro = ${it[Rolls.xpro]}",
                        "nonXa = ${it[Rolls.nonxa]}",
                        it[Pics.hashtags].toString().split(',').map { t-> "tag = ${t.trim()}" }.sorted().toString().drop(1).dropLast(1),
                    ).toString()
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

    override suspend fun getRollThumbs(baseUrl: String): List<Thumb> {
        val query = Rolls.selectAll()
        return dbQuery {
            query.toList().map {
                val rollNumber = it[Rolls.id].value
                Thumb(
                    RollEntity[rollNumber].title,
                    baseUrl + RollEntity[rollNumber].frames.toList().random().imageUrl
                )
            }
        }
    }

    override suspend fun editCollection(theUserId: Int, theCollection: String?, picId: Int?) {
        var isSuccess = false
        transaction {
            val thePic = PicEntity.findById(picId ?: 0)
            val coll = CollectionEntity.find {
                (Collections.userId eq (theUserId) and
                        (Collections.title eq (theCollection ?: "") and
                                (Collections.pic eq picId)))
            }.firstOrNull()

            // if element found -> remove, else add:
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

    override suspend fun renameOrDeleteCollection(theUserId: Int, theCollectionTitle: String, theRenamedTitle: String?) {
        transaction {
            val coll = CollectionEntity.find {
                (Collections.userId eq (theUserId) and
                        (Collections.title eq (theCollectionTitle)))
            }
            coll.forEach{
                if (theRenamedTitle != null) {
                    it.title = theRenamedTitle
                } else {
                    it.delete()
                }
            }
        }
    }

    override suspend fun getAllCollections(theUserId: Int, baseUrl: String): List<Thumb> {
        var query: List<ResultRow> = emptyList()
        transaction {
            query = (Collections innerJoin Pics)
                .select { Collections.userId eq theUserId }
                .reversed()
                .distinctBy { it[Collections.title] }
        }

        return dbQuery {
            query.map {
                Thumb(it[Collections.title], baseUrl + it[Pics.imageUrl])
            }
        }
    }

    override suspend fun getCollection(userId: Int, collection: String, baseUrl: String): List<Pic> {
        val query = (Collections innerJoin Pics)
            .select { Collections.userId eq userId }
            .andWhere { Collections.title eq collection }

        return dbQuery {
            query.map {
                Pic(
                    it[Pics.id].value,
                    baseUrl + it[Pics.imageUrl],
                    it[Pics.description],
                    listOf(
                        "year = ${it[Pics.year]}",
                        "filmName = ${it[Films.filmName]}",
                        "filmType = ${it[Films.type]}",
                        "iso = ${it[Films.iso]}",
                        "expired = ${it[Rolls.expired]}",
                        "xpro = ${it[Rolls.xpro]}",
                        "nonXa = ${it[Rolls.nonxa]}",
                        it[Pics.hashtags].toString().split(',').map { t-> "tag = ${t.trim()}" }.sorted().toString().drop(1).dropLast(1),
                    ).toString()
                )
//                transaction {
//                    val pic = PicEntity.findById(it[Collections.pic]) // TODO null-check
//                    Pic(
//                        pic!!.id.value,
//                        pic.year,
//                        pic.description,
//                        baseUrl + pic.imageUrl,
//                        pic.tags,
//                        pic.roll.film.filmName,
//                        pic.roll.film.type,
//                        pic.roll.film.iso,
//                        pic.roll.expired,
//                        pic.roll.xpro,
//                        pic.roll.nonXa
//                    )
//                }
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

    override suspend fun getRandomPic(baseUrl: String): Pic {
        val log = LoggerFactory.getLogger(this.javaClass)
        val query = (Films innerJoin Rolls innerJoin Pics).selectAll()
        return dbQuery {
            val pic = query.toList().random()
            log.debug("Pic url = ${pic[Pics.imageUrl]}")
            Pic(
                pic[Pics.id].value,
                baseUrl + pic[Pics.imageUrl],
                pic[Pics.description],
                ""
//                pic[Pics.id].value,
//                pic[Pics.year],
//                pic[Pics.description],
//                baseUrl + pic[Pics.imageUrl],
//                pic[Pics.tags],
//                pic[Films.filmName],
//                pic[Films.type],
//                pic[Films.iso],
//                pic[Rolls.expired],
//                pic[Rolls.xpro],
//                pic[Rolls.nonxa]
            )
        }
    }

    override suspend fun getAllTags(): String {
        val log = LoggerFactory.getLogger(this.javaClass)
        val filmsQuery = Films.selectAll()
        val picsQuery = Pics.selectAll()
        return dbQuery {
            val filmsList = filmsQuery.toList()

            val filmNames = filmsList.map {
                "filmName = ${it[Films.filmName]}"
            }.sorted()
//            log.debug("filmss = $filmNames")

            val types = filmsList.map {
                "filmType = ${it[Films.type]}"
            }.distinct().sorted()

            val iso = filmsList.map {
                "iso = ${it[Films.iso]}"
            }.distinct().sorted()

            val rollAttributes = listOf("expired = false, expired = true, xpro = false, xpro = true, nonXa = false, nonXa = true")

            val picsList = picsQuery.toList()

            val years = picsList.map {
                "year = ${it[Pics.year]}"//.toString()
            }.distinct().sorted()

            val hashtags = picsList.map { row ->
                row[Pics.hashtags].toString().split(',').map { "hashtag = ${it.trim()}" }
            }.flatten().distinct().sorted()

            listOf(
                types, rollAttributes, filmNames, iso, years, hashtags
            ).flatten().toString().drop(1).dropLast(1)
        }
    }

}