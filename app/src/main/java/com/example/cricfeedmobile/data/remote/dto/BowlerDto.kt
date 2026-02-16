package com.example.cricfeedmobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BowlerDto(
    @SerialName("name") val name: String,
    @SerialName("overs") val overs: String,
    @SerialName("maidens") val maidens: Int,
    @SerialName("runs") val runs: Int,
    @SerialName("wickets") val wickets: Int
)
