package com.example.cricfeedmobile.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable


@Serializable data object Home : NavKey
@Serializable data object MatchResults : NavKey

// Root screen of the Home tab's own nested navigation
@Serializable data object HomeRoot : NavKey

// Pushed onto the Home tab's back stack from HomeRoot
@Serializable data object UpcomingMatches : NavKey