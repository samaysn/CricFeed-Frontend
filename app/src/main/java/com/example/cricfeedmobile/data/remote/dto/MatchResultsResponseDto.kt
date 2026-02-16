package com.example.cricfeedmobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MatchResultsResponseDto(
    @SerialName("results") val results: List<MatchResultDto>,
    @SerialName("pagination") val pagination: PaginationDto
)