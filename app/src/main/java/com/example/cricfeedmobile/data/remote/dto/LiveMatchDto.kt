package com.example.cricfeedmobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LiveMatchDto(
    @SerialName("matchId") val matchId: Int,
    @SerialName("title") val title: String,
    @SerialName("matchType") val matchType: String,
    @SerialName("venue") val venue: String,
    @SerialName("status") val status: String,
    @SerialName("seriesName") val seriesName: String,
    @SerialName("team1") val team1: TeamScoreDto,
    @SerialName("team2") val team2: TeamScoreDto,
    @SerialName("liveText") val liveText: String,
    @SerialName("currentBatsmen") val currentBatsmen: List<BatsmanDto>? = null,
    @SerialName("currentBowler") val currentBowler: BowlerDto? = null,
    @SerialName("lastWicket") val lastWicket: String? = null,
    @SerialName("recentBalls") val recentBalls: List<String> = emptyList(),
    @SerialName("startedAt") val startedAt: String
)
