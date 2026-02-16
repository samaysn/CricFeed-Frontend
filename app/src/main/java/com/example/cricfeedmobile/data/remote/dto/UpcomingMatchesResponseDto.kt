package com.example.cricfeedmobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpcomingMatchesResponseDto(
    @SerialName("matches") val matches: List<UpcomingMatchDto>,
    @SerialName("pagination") val pagination: PaginationDto
)