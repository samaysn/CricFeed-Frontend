package com.example.cricfeedmobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BatsmanDto(
    @SerialName("name") val name: String,
    @SerialName("runs") val runs: Int,
    @SerialName("balls") val balls: Int,
    @SerialName("fours") val fours: Int,
    @SerialName("sixes") val sixes: Int
)
