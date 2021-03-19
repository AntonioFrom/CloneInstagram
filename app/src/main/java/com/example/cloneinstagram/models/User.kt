package com.example.cloneinstagram.models

data class User(
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val website: String? = null,
    val bio: String? = null,
    val phone: String? = null,
    val photo: String? = null
)
