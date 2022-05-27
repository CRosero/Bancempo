package com.bancempo.data

data class User(
    val fullname: String,
    val nickname: String,
    val description: String,
    val location: String,
    val email: String,
    val skills: List<String>,
    val imageUser: String
) {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        listOf(),
        "",
    )
}

