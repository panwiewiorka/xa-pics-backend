package com.xapics.data.models

import kotlinx.serialization.Serializable

@Serializable
data class TheString(
    val string: String
)

@Serializable
data class AuthRequest(
    val username: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String?
)

@Serializable
data class User(
    val username: String,
    val password: String,
    val salt: String,
    val id: Int? = null
)

@Serializable
data class Film(
    val filmName: String = "",
    val iso: Int? = null,
    val type: FilmType = FilmType.NULL,
)

@Serializable
data class Roll(
    val title: String,
    val film: String,
//    val date: Date,
    val expired: Boolean = false,
    val xpro: Boolean = false,
//    val nonXa: Boolean = false,
)

@Serializable
data class Pic(
    val id: Int,
    val imageUrl: String,
    val description: String,
    val tags: String
)

@Serializable
data class Thumb(
    val title: String,
    val thumbUrl: String
)

enum class FilmType { SLIDE, NEGATIVE, BW, NULL }

data class Tag(
    val type: String,
    val value: String
)