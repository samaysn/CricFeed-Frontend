package com.example.cricfeedmobile.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
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
    val resultBackStack = rememberNavBackStack(MatchResultsRoot)
    val currentTab = rootBackStack.lastOrNull()

    val saveableStateHolder = rememberSaveableStateHolder()
    Scaffold(
        bottomBar = {
            // Bottom bar is visible only when:
            // • We are on a bottom-nav tab  AND
            // • The Home tab is NOT deep inside UpcomingMatches
            val showBottomBar = when (currentTab) {
                Home       -> homeBackStack.lastOrNull() == HomeRoot
                MatchResults -> true
                else       -> false
            }

            if (showBottomBar && currentTab != null) {
                CricFeedBottomNavBar(
                    currentKey = currentTab,
                    onTabSelected = { selectedKey ->
                        if (currentTab != selectedKey) {
                            // Swap the active tab — clear and replace
                            rootBackStack.clear()
                            rootBackStack.add(selectedKey)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()).fillMaxSize())
            {

            saveableStateHolder.SaveableStateProvider(
                key = "home"
            ) {
                // ── Home Tab — renders HomeNestedFlow which has its own NavDisplay ──
                HomeNestedFlow(homeBackStack = homeBackStack, visible = currentTab == Home)
            }

                saveableStateHolder.SaveableStateProvider(
                    key = "results"
                ) {
                    // ── Match Results Tab ──────────────────────────────────────────────
                    ResultsNestedFlow(resultBackStack = resultBackStack,visible = currentTab == MatchResults )
                }

            }

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
fun HomeNestedFlow(homeBackStack: NavBackStack<NavKey>, visible : Boolean) {
    if (!visible) return
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


@Composable
fun ResultsNestedFlow(resultBackStack : NavBackStack<NavKey>, visible : Boolean){
    if (!visible) return
    NavDisplay(
        backStack = resultBackStack,
        onBack = {
            if(resultBackStack.size > 1) resultBackStack.removeLastOrNull()
        },
        entryProvider = entryProvider {
            entry<MatchResultsRoot> {
                MatchResultsScreen(viewModel = hiltViewModel())
            }
        }
    )
}