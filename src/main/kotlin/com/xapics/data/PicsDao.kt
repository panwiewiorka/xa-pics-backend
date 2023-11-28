package com.xapics.data

import com.xapics.data.models.Film
import com.xapics.data.models.FilmPic

interface PicsDao {
    suspend fun getFilmPicsList(year: Int?, film: String?): List<FilmPic>
    suspend fun getFilmsList(): List<Film>
}