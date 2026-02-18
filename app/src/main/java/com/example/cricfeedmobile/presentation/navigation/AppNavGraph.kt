package com.example.cricfeedmobile.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.cricfeedmobile.presentation.home.HomeScreen
import com.example.cricfeedmobile.presentation.matchResults.MatchResultsScreen
import com.example.cricfeedmobile.presentation.upcoming.UpcomingMatchesScreen

// Which NavKeys show the bottom bar
private val bottomNavKeys: Set<NavKey> = setOf(Home, MatchResults)

@Composable
fun AppNavGraph() {
    val backStack = rememberNavBackStack(Home)
    val currentKey = backStack.lastOrNull()

    val showBottomBar = currentKey in bottomNavKeys

    Scaffold(
        bottomBar = {
            if (showBottomBar && currentKey != null) {
                CricFeedBottomNavBar(
                    currentKey = currentKey,
                    onTabSelected = { selectedKey ->
                        if (currentKey != selectedKey) {

                            val existingIndex = backStack.indexOfLast { it == selectedKey }
                            if (existingIndex >= 0) {

                                backStack.subList(existingIndex + 1, backStack.size).clear()
                            } else {

                                backStack.add(selectedKey)
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {

                // ── Home Tab ──────────────────────────────────────────────
                entry<Home> {
                    HomeScreen(
                        viewModel = hiltViewModel(),
                        onNavigateToUpcoming = {
                            backStack.add(UpcomingMatches)
                        }
                    )
                }

                // ── Match Results Tab ─────────────────────────────────────
                entry<MatchResults> {
                    MatchResultsScreen(
                        viewModel = hiltViewModel()
                    )
                }

                // ── Standalone Screen (no bottom bar) ─────────────────────
                entry<UpcomingMatches> {
                    UpcomingMatchesScreen(
                        homeViewModel = hiltViewModel(),
                        onBackClick = { backStack.removeLastOrNull() }
                    )
                }

            }
        )
    }
}