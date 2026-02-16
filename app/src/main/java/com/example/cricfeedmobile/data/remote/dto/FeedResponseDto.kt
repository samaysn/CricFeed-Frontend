package com.example.cricfeedmobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedResponseDto(
    @SerialName("feed") val data: List<FeedItemDto>,
    @SerialName("meta") val meta: MetaDto,
    @SerialName("pagination") val pagination: PaginationDto,
)