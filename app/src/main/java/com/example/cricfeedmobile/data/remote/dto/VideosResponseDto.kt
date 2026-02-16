package com.example.cricfeedmobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideosResponseDto(
    @SerialName("videos") val videos: List<VideoDto>,
    @SerialName("pagination") val pagination: PaginationDto
)