package com.xapics.data.models

import kotlinx.serialization.Serializable

data class Pic(   // TODO delete?
    val year: Int,
    val description: String,
    val imageUrl: String
)

@Serializable
data class Film(
    val name: String = "",
    val iso: Int? = null,
    val type: FilmType = FilmType.NULL,
    val xpro: Boolean = false,
    val expired: Boolean = false,
)

@Serializable
data class Roll(
    val title: String,
    val film: Film,
//    val date: Date,
    val nonXa: Boolean = false,
)

@Serializable
data class FilmPic(
    val year: Int,
    val description: String,
    val imageUrl: String,
    val film: String
)

enum class FilmType { SLIDE, NEGATIVE, BW, NULL }