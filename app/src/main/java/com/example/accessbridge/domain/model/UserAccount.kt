package com.example.accessbridge.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserAccount(
    val id: String,
    val email: String,
    val name: String = ""
)
