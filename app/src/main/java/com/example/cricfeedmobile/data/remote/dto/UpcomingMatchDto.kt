package com.example.cricfeedmobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpcomingMatchDto(
    @SerialName("matchId") val matchId: Int,
    @SerialName("title") val title: String,
    @SerialName("venue") val venue: String,
    @SerialName("startTime") val startTime: String,
    @SerialName("team1") val team1: TeamDto,
    @SerialName("team2") val team2: TeamDto,
    @SerialName("matchType") val matchType: String,
    @SerialName("seriesName") val seriesName: String,
    @SerialName("isNotificationSet") val isNotificationSet: Boolean
)
