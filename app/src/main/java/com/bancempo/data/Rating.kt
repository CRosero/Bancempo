package com.bancempo.data

data class Rating(
    val idAuthor: String,
    val idReceiver: String,
    val idAdv: String,
    val rating: Double,
    val ratingText: String
) {
    constructor() : this(
        "",
        "",
        "",
        0.0,
        ""
    )
}



