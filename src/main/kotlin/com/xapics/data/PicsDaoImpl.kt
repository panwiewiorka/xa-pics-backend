package com.xapics.data

import com.xapics.data.DatabaseFactory.dbQuery
import com.xapics.data.models.*
import com.xapics.data.models.FilmType.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

class PicsDaoImpl : PicsDao {

    private fun ResultRow.toPic(): Pic {
        return Pic(
            this[Pics.id].value,
            this[Pics.imageUrl],
            this[Pics.description],
            listOf(
                "year = ${this[Pics.year]}",
                "filmName = ${this[Films.filmName]}",
                "filmType = ${this[Films.type]}",
                "iso = ${this[Films.iso]}",
                "expired = ${this[Rolls.expired]}",
                "xpro = ${this[Rolls.xpro]}",
                "nonXa = ${this[Rolls.nonxa]}",
                this[Pics.hashtags].toString().split(',').map { "hashtag = ${it.trim()}" }.sorted().toString().drop(1).dropLast(1)
            ).toString().drop(1).dropLast(1)
        )
    }

    override suspend fun getPicsList(theQuery: String): List<Pic> {
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
//                "search" -> { query.andWhere { Pics.description like "%${tag.value}%" } } // TODO search all? here?
            }
        }

        return dbQuery {
            query.map {
                it.toPic()
            }
        }
    }

    override suspend fun getSearchResponse(searchQuery: String): List<Pic> {
        val log = LoggerFactory.getLogger(this.javaClass)

        val tagGroups = searchQuery
            .split(", ")
            .map { it.split(" = ") }
            .map { Tag(it[0], it[1]) }.groupBy { it.type }

        val query = (Films innerJoin Rolls innerJoin Pics).selectAll()
        val hashTagsQuery = (Films innerJoin Rolls innerJoin Pics).selectAll()

        transaction {
            tagGroups.forEach { group ->
                val tagValues = group.value.map { it.value }
                when (group.key) {
                    "search" -> {
                        val searchWords = tagValues.toString().drop(1).dropLast(1).split("\\s+".toRegex())
                        searchWords.forEach {
                            query
                                .orWhere { Pics.description like "%$it%" }
//                                .orWhere { Pics.hashtags like "%$it%" }
//                                .orWhere { Rolls.title like "%$it%" }
//                                .orWhere { Films.filmName like "%$it%" }
                        }
                    }
                    "filmType" -> {
                        val types = tagValues.map {
                            when (it) {
                                "SLIDE" -> SLIDE
                                "NEGATIVE" -> NEGATIVE
                                "BW" -> BW
                                else -> NULL
                            }
                        }
                        query.andWhere { Films.type inList types }
                    }
                    "roll" -> { query.andWhere { Rolls.title inList tagValues } }
                    "nonXa" -> { query.andWhere { Rolls.nonxa inList tagValues.map { it == "true" } } }
                    "expired" -> { query.andWhere { Rolls.expired inList tagValues.map { it == "true" } } }
                    "xpro" -> { query.andWhere { Rolls.xpro inList tagValues.map { it == "true" } } }
                    "iso" -> { query.andWhere { Films.iso inList tagValues.map { it.toInt() } } }
                    "filmName" -> { query.andWhere { Films.filmName inList tagValues } }
                    "year" -> { query.andWhere { Pics.year inList tagValues.map { it.toInt() } } }
                    "hashtag" -> {
                        val hashtags = tagValues.toString().drop(1).dropLast(1).split(", ")
                        hashtags.forEach {
                            hashTagsQuery.orWhere { Pics.hashtags like "%$it%" }
                        }
                    }
                }
            }
        }

        return dbQuery {
            query.intersect(hashTagsQuery).toList().map {
                it.toPic()
            }.distinct()
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

    override suspend fun getRollThumbs(): List<Thumb> {
        val query = Rolls.selectAll()
        return dbQuery {
            query.toList().map {
                val rollNumber = it[Rolls.id].value
                Thumb(
                    RollEntity[rollNumber].title,
                    RollEntity[rollNumber].frames.toList().random().imageUrl
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

    override suspend fun getAllCollections(theUserId: Int): List<Thumb> {
        var query: List<ResultRow> = emptyList()
        transaction {
            query = (Collections innerJoin Pics)
                .select { Collections.userId eq theUserId }
                .reversed()
                .distinctBy { it[Collections.title] }
        }

        return dbQuery {
            query.map {
                Thumb(it[Collections.title], it[Pics.imageUrl])
            }
        }
    }

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
                        pic.imageUrl,
                        pic.description,
                        listOf(
                            "year = ${pic.year}",
                            "filmName = ${pic.roll.film.filmName}",
                            "filmType = ${pic.roll.film.type}",
                            "iso = ${pic.roll.film.iso}",
                            "expired = ${pic.roll.expired}",
                            "xpro = ${pic.roll.xpro}",
                            "nonXa = ${pic.roll.nonXa}",
                            pic.hashtags.split(',').map { "hashtag = ${it.trim()}" }.sorted().toString().drop(1).dropLast(1),
                        ).toString().drop(1).dropLast(1)
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

    override suspend fun getRandomPic(): Pic {
        val query = (Films innerJoin Rolls innerJoin Pics).selectAll()
        return dbQuery {
            query.toList().random().toPic()
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

            val rollAttributes = listOf("expired = false, expired = true, xpro = false, xpro = true, nonXa = false, nonXa = true") // TODO move to frontend?

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

    override suspend fun getFilteredTags(theQuery: String): String {
        val log = LoggerFactory.getLogger(this.javaClass)

        val tagGroups = theQuery
            .split(", ")
            .map { it.split(" = ") }
            .map { Tag(it[0], it[1]) }.groupBy { it.type }

        val query = (Films innerJoin Rolls innerJoin Pics).selectAll()
        val hashTagsQuery = (Films innerJoin Rolls innerJoin Pics).selectAll()

        transaction {
            tagGroups.forEach { group ->
                val tagValues = group.value.map { it.value }
                when (group.key) {
                    "filmType" -> {
                        val types = tagValues.map {
                            when (it) {
                                "SLIDE" -> SLIDE
                                "NEGATIVE" -> NEGATIVE
                                "BW" -> BW
                                else -> NULL
                            }
                        }
                        query.andWhere { Films.type inList types }
                    }
                    "roll" -> { query.andWhere { Rolls.title inList tagValues } }
                    "nonXa" -> { query.andWhere { Rolls.nonxa inList tagValues.map { it == "true" } } }
                    "expired" -> { query.andWhere { Rolls.expired inList tagValues.map { it == "true" } } }
                    "xpro" -> { query.andWhere { Rolls.xpro inList tagValues.map { it == "true" } } }
                    "iso" -> { query.andWhere { Films.iso inList tagValues.map { it.toInt() } } }
                    "filmName" -> { query.andWhere { Films.filmName inList tagValues } }
                    "year" -> { query.andWhere { Pics.year inList tagValues.map { it.toInt() } } }
                    "hashtag" -> {
                        val hashtags = tagValues.toString().drop(1).dropLast(1).split(", ")
                        hashtags.forEach {
                            hashTagsQuery.orWhere { Pics.hashtags like "%$it%" }
                        }
                    }
                }
            }
        }

        return dbQuery {

            val rollAttributes = listOf("expired = false, expired = true, xpro = false, xpro = true, nonXa = false, nonXa = true") // TODO move to frontend?

            val picsList = query.intersect(hashTagsQuery).toList()

            val films = picsList.map {
                "filmName = ${it[Films.filmName]}"
            }.distinct().sorted()

            val types = picsList.map {
                "filmType = ${it[Films.type]}"
            }.distinct().sorted()

            val iso = picsList.map {
                "iso = ${it[Films.iso]}"
            }.distinct().sorted()

            val expired = picsList.map {
                "expired = ${it[Rolls.expired]}"
            }.distinct().sorted()

            val nonxa = picsList.map {
                "nonXa = ${it[Rolls.nonxa]}"
            }.distinct().sorted()

            val xpro = picsList.map {
                "xpro = ${it[Rolls.xpro]}"
            }.distinct().sorted()

            val years = picsList.map {
                "year = ${it[Pics.year]}"
            }.distinct().sorted()

            val hashtags = picsList.map { row ->
                row[Pics.hashtags].toString().split(',').map { "hashtag = ${it.trim()}" }
            }.flatten().distinct().sorted()

            log.debug("filmss = ${listOf(
                types, rollAttributes, iso, films, years, hashtags
            ).flatten().toString().drop(1).dropLast(1)}")

            listOf(
                types, expired, xpro, nonxa, iso, films, years, hashtags
            ).flatten().toString().drop(1).dropLast(1)
        }
    }

}