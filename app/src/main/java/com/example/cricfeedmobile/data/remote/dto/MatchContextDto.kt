package com.example.cricfeedmobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MatchContextDto(
    @SerialName("matchId") val matchId: String,
    @SerialName("title") val title: String? ,
)