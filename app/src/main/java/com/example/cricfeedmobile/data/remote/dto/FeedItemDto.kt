package com.example.cricfeedmobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class FeedItemDto(
   @SerialName("type") val type: String,
   @SerialName("id") val id: String,
   @SerialName("timestamp") val timestamp: Long,
   @SerialName("priority") val priority: Int? = null,
   @SerialName("data") val data: JsonElement
)
