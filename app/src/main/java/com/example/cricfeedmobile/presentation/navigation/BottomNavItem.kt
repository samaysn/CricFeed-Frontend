package com.example.cricfeedmobile.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem (
    val route: String,
    val label: String,
    val icon: ImageVector
){

    object Home : BottomNavItem(
        route = Routes.HOME,
        label = "Home",
        icon = Icons.Default.Home
    )

    object MatchResults : BottomNavItem(
        route = Routes.MATCH_RESULTS,
        label = "Results",
        icon = Icons.Default.List
    )

}