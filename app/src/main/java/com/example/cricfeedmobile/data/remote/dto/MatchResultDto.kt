package com.example.cricfeedmobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MatchResultDto(
    @SerialName("matchId") val matchId: String,
    @SerialName("title") val title: String,
    @SerialName("matchType") val matchType: String,
    @SerialName("team1") val team1: TeamDto,
    @SerialName("team2") val team2: TeamDto,
    @SerialName("result") val result: String,
    @SerialName("playerOfMatch") val playerOfMatch: PlayerDto? = null,
    @SerialName("completedAt") val completedAt: String,
    @SerialName("venue") val venue : String? = "Unknown"
)
