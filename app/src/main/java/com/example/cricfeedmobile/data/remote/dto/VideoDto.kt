package com.example.cricfeedmobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoDto(
    @SerialName("videoId") val videoId: Int,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("thumbnail") val thumbnailUrl: String,
    @SerialName("duration") val duration: String,
    @SerialName("durationSeconds") val durationSeconds: Int? = null,
    @SerialName("views") val views: Int,
    @SerialName("uploadedAt") val uploadedAt: String,
    @SerialName("videoUrl") val videoUrl: String,
    @SerialName("type") val videoType: String? = null,
    @SerialName("matchContext") val matchContext: MatchContextDto? = null
)