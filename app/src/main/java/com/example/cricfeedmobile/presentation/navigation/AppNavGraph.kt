package com.example.cricfeedmobile.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.cricfeedmobile.presentation.home.HomeScreen
import com.example.cricfeedmobile.presentation.matchResults.MatchResultsScreen
import com.example.cricfeedmobile.presentation.upcoming.UpcomingMatchesScreen

@Composable
fun AppNavGraph() {
    // Root back stack — only ever holds the active tab key (Home or MatchResults)
    val rootBackStack = rememberNavBackStack(Home)

    // Home tab's own back stack, lifted here so AppNavGraph can read its top
    // entry and hide/show the bottom bar when UpcomingMatches is open
    val homeBackStack = rememberNavBackStack(HomeRoot)

    Scaffold(
        bottomBar = {
            val currentKey = rootBackStack.lastOrNull()

            // Bottom bar is visible only when:
            // • We are on a bottom-nav tab  AND
            // • The Home tab is NOT deep inside UpcomingMatches
            val showBottomBar = when (currentKey) {
                Home       -> homeBackStack.lastOrNull() == HomeRoot
                MatchResults -> true
                else       -> false
            }

            if (showBottomBar && currentKey != null) {
                CricFeedBottomNavBar(
                    currentKey = currentKey,
                    onTabSelected = { selectedKey ->
                        if (currentKey != selectedKey) {
                            // Swap the active tab — clear and replace
                            rootBackStack.clear()
                            rootBackStack.add(selectedKey)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = rootBackStack,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            onBack = { /* tabs don't back-navigate into each other */ },
            entryProvider = entryProvider {

                // ── Home Tab — renders HomeNestedFlow which has its own NavDisplay ──
                entry<Home> {
                    HomeNestedFlow(homeBackStack = homeBackStack)
                }

                // ── Match Results Tab ──────────────────────────────────────────────
                entry<MatchResults> {
                    MatchResultsScreen(viewModel = hiltViewModel())
                }

            }
        )
    }
}

/**
 * The Home tab's own nested navigation.
 * homeBackStack is owned by AppNavGraph so the bottom bar can observe it.
 *
 * Stack states:
 *   [HomeRoot]                  → HomeScreen (bottom bar visible)
 *   [HomeRoot, UpcomingMatches] → UpcomingMatchesScreen (bottom bar hidden)
 */
@Composable
fun HomeNestedFlow(homeBackStack: NavBackStack<NavKey>) {
    NavDisplay(
        backStack = homeBackStack,
        onBack = {
            // Only pop if there is something above HomeRoot to pop
            if (homeBackStack.size > 1) homeBackStack.removeLastOrNull()
        },
        entryProvider = entryProvider {

            entry<HomeRoot> {
                HomeScreen(
                    viewModel = hiltViewModel(),
                    onNavigateToUpcoming = { homeBackStack.add(UpcomingMatches) }
                )
            }

            entry<UpcomingMatches> {
                UpcomingMatchesScreen(
                    homeViewModel = hiltViewModel(),
                    onBackClick = { homeBackStack.removeLastOrNull() }
                )
            }

        }
    )
}