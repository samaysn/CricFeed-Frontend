package com.example.cricfeedmobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsResponseDto(
    @SerialName("articles") val articles: List<NewsDto>,
    @SerialName("pagination") val pagination: PaginationDto
)