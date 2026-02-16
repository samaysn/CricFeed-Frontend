package com.example.cricfeedmobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsDto(
    @SerialName("articleId") val articleId: String,
    @SerialName("headline") val headline: String,
    @SerialName("summary") val summary: String,
    @SerialName("imageUrl") val thumbnailUrl: String? = null,
    @SerialName("author") val author: AuthorDto,
    @SerialName("publishedAt") val publishedAt: String,
    @SerialName("category") val category: String,
    @SerialName("readTimeMinutes") val readTime: String? = null
)