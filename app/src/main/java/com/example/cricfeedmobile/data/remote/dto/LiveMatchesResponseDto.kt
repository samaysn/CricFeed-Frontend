package com.example.cricfeedmobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LiveMatchesResponseDto(
    @SerialName("liveMatches") val liveMatches: List<LiveMatchDto>,
    @SerialName("count") val count: Int
)