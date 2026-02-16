package com.example.cricfeedmobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlayerDto(
    @SerialName("playerId") val playerId: String? = null,
    @SerialName("name") val name: String,
    @SerialName("image") val avatarUrl: String? = null
)