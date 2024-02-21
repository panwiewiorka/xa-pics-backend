package com.xapics.data

import com.xapics.data.models.Thumb
import com.xapics.data.models.Film
import com.xapics.data.models.Pic
import com.xapics.data.models.Roll

interface PicsDao {
    suspend fun getPicsList(year: Int?, roll: String?, tag: String?, film: String?, description: String?, baseUrl: String): List<Pic>
    suspend fun getSearchResponse(searchQuery: String, baseUrl: String): List<Pic>
    suspend fun getFilmsList(): List<Film>
    suspend fun getRollsList(): List<Roll>
    suspend fun getRollThumbs(baseUrl: String): List<Thumb>
    suspend fun editCollection(theUserId: Int, theCollection: String?, picId: Int?)
    suspend fun renameOrDeleteCollection(theUserId: Int, theCollectionTitle: String, theRenamedTitle: String?)
    suspend fun getAllCollections(theUserId: Int, baseUrl: String): List<Thumb>
    suspend fun getCollection(userId: Int, collection: String, baseUrl: String): List<Pic>
    suspend fun getPicCollections(userId: Int, picId: Int): List<String>
    suspend fun getRandomPic(baseUrl: String): Pic
    suspend fun getAllTags(): List<List<String>>
}