package com.xapics.data

import com.xapics.data.models.Film
import com.xapics.data.models.FilmPic
import com.xapics.data.models.Roll

interface PicsDao {
    suspend fun getFilmPicsList(year: Int?, roll: String?, film: String?): List<FilmPic>
    suspend fun getFilmsList(): List<Film>
    suspend fun getRollsList(): List<Roll>
    suspend fun getRollThumbs(): List<Pair<String, String>>
}