package com.example.cricfeedmobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpcomingMatchesCarouselDto(
   @SerialName("totalCount") val totalCount: Int,
    @SerialName("matches") val matches: List<UpcomingMatchDto>
)
