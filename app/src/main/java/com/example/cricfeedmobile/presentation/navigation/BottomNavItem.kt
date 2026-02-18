package com.example.cricfeedmobile.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey

sealed class BottomNavItem (
    val key : NavKey,
    val label: String,
    val icon: ImageVector
){

    object HomeTab : BottomNavItem(
        key = Home,
        label = "Home",
        icon = Icons.Default.Home
    )

    object MatchResultsTab : BottomNavItem(
        key = MatchResults,
        label = "Results",
        icon = Icons.Default.List
    )

}