package com.example.cricfeedmobile.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable


@Serializable data object Home : NavKey
@Serializable data object MatchResults : NavKey

@Serializable data object UpcomingMatches : NavKey