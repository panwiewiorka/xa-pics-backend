package com.xapics.data.models

import kotlinx.serialization.Serializable

const val BASE_URL = "http://192.168.0.87:8080"


@Serializable
data class Film(
    val filmName: String = "",
    val iso: Int? = null,
    val type: FilmType = FilmType.NULL,
    val xpro: Boolean = false,
    val expired: Boolean = false,
)

@Serializable
data class Roll(
    val title: String,
    val film: String,
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