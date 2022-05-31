package com.bancempo.data

data class Rating(
    val idAdv: String,
    val idAsker: String,
    val idBidder: String,
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



