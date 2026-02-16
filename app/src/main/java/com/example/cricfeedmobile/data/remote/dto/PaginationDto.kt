package com.example.cricfeedmobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaginationDto(
    @SerialName("currentPage") val currentPage: Int,
    @SerialName("totalPages") val totalPages: Int,
    @SerialName("totalItems") val totalItems: Int,
    @SerialName("itemsPerPage") val itemsPerPage: Int,
    @SerialName("hasNext") val hasNext: Boolean,
    @SerialName("hasPrevious") val hasPrevious: Boolean,
    @SerialName("isSpecialPage") val isSpecialPage : Boolean? = false
)