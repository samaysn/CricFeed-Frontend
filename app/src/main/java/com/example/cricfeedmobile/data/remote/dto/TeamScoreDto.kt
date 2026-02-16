package com.example.cricfeedmobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamScoreDto(
    @SerialName("name") val name: String,
    @SerialName("shortName") val shortName: String,
    @SerialName("logo") val logo: String,
    @SerialName("score") val score: String,
    @SerialName("overs") val overs: String,
    @SerialName("runRate") val runRate: String? = null
)
