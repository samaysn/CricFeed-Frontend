package com.example.cricfeedmobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BannerAdDto(
    @SerialName("adId") val adId: String,
    @SerialName("title") val title: String? = null,
    @SerialName("subtitle") val subtitle: String? = null,
    @SerialName("imageUrl") val imageUrl: String,
    @SerialName("deepLink") val targetUrl: String,
    @SerialName("sponsor") val sponsor: String? = null,
    @SerialName("priority") val priority: Int? = null
)
