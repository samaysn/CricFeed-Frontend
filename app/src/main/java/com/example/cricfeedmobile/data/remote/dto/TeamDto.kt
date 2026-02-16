package com.example.cricfeedmobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamDto(
    @SerialName("name") val name: String,
    @SerialName("shortName") val shortName: String,
    @SerialName("logo") val logo: String,
    @SerialName("scores") val scores: List<String?> = emptyList(),
)
