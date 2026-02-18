package com.example.cricfeedmobile.presentation.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.Paragraph
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.launch

@Composable
fun CricFeedBottomNavBar (
    currentKey : NavKey,
    onTabSelected : (NavKey) -> Unit
){
    val navItems = listOf(
        BottomNavItem.HomeTab,
        BottomNavItem.MatchResultsTab
    )

    NavigationBar {

        navItems.forEach { item ->
            NavigationBarItem(
                selected = currentKey == item.key,
                onClick = {
                    onTabSelected(item.key)
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        item.label
                    )
                }
            )
        }

    }



}