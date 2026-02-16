package com.example.cricfeedmobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MetaDto(
    @SerialName("generatedAt") val timestamp: String,
    @SerialName("version") val apiVersion: String,
//    @SerialName("environment") val environment: String,
)