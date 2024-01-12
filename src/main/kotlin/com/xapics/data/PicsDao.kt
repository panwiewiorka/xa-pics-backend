package com.xapics.data

import com.xapics.data.models.Film
import com.xapics.data.models.Pic
import com.xapics.data.models.Roll

interface PicsDao {
    suspend fun getPicsList(year: Int?, roll: String?, tag: String?, film: String?): List<Pic>
    suspend fun getFilmsList(): List<Film>
    suspend fun getRollsList(): List<Roll>
    suspend fun getRollThumbs(): List<Pair<String, String>>
    suspend fun editCollection(theUserId: Int, theCollection: String?, picId: Int?)
    suspend fun getAllCollections(theUserId: Int): List<Pair<String, String>>
//    suspend fun createCollection(theUserId: Int, theTitle: String, picId: Int)
    suspend fun getCollection(userId: Int, collection: String): List<Pic>
    suspend fun getPicCollections(userId: Int, picId: Int): List<String>
}