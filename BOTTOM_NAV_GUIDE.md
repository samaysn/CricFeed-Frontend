# Bottom Navigation Guide

**Project:** CricFeed Android App
**Goal:** Add a bottom nav bar with 2 tabs — Home Feed and Match Results

---

## Table of Contents

1. [Navigation Theory](#navigation-theory)
2. [Stack Navigation vs Bottom Navigation](#stack-navigation-vs-bottom-navigation)
3. [Key Navigation Concepts](#key-navigation-concepts)
4. [Architecture of What We're Building](#architecture-of-what-were-building)
5. [Step-by-Step Implementation](#step-by-step-implementation)
6. [How It All Connects](#how-it-all-connects)
7. [Common Mistakes](#common-mistakes)

---

## Navigation Theory

### What is a NavController?

`NavController` is the **central coordinator** of all navigation in your app. Think of it as the traffic controller at an airport — it knows where every "flight" (screen) is, what the history looks like, and where to go next.

```kotlin
val navController = rememberNavController()
```

It handles:
- **Navigate forward**: push a new screen onto the back stack
- **Navigate back**: pop the current screen off the back stack
- **Navigate to a tab**: switch the entire visible destination

There is **one** `NavController` per navigation graph. In our case, one for the entire app.

---

### What is a NavHost?

`NavHost` is the **container** — the area of the screen that actually displays the current destination. It's the "frame" that swaps screens in and out.

```kotlin
NavHost(
    navController = navController,
    startDestination = Routes.HOME
) {
    // All screen definitions go here — this IS the NavGraph
}
```

It is NOT a visual component by itself. It's a Composable that delegates to whichever screen the NavController is currently pointing at.

---

### What is a NavGraph?

The NavGraph is the **map** of all possible destinations and how they connect. In Compose, you define it using the `NavGraphBuilder` DSL inside the `NavHost` block:

```kotlin
NavHost(...) {
    // Each composable() call = one node in the graph
    composable(Routes.HOME) { HomeScreen(...) }
    composable(Routes.MATCH_RESULTS) { MatchResultsScreen(...) }
    composable(Routes.UPCOMING_MATCHES) { UpcomingMatchesScreen(...) }
}
```

The graph answers the question: **"What screens exist and how do I get to them?"**

---

### What is the Back Stack?

The back stack is a **LIFO stack** (Last In, First Out) of screen destinations. Every time you navigate forward, a destination is pushed. Every time you go back, one is popped.

```
// User flow:
Home (start)
  → navigate(UPCOMING_MATCHES)        Back stack: [HOME, UPCOMING_MATCHES]
  → navigateUp()                      Back stack: [HOME]
  → navigate(UPCOMING_MATCHES) again  Back stack: [HOME, UPCOMING_MATCHES]
```

Pressing the Android back button calls `navigateUp()`, which pops the top of the stack.

**Back stack visualization for our current app:**
```
[HOME]
  ↓ navigate(UPCOMING_MATCHES)
[HOME → UPCOMING_MATCHES]
  ↓ back button
[HOME]
```

---

## Stack Navigation vs Bottom Navigation

### Stack Navigation (What We Have Now)

Stack navigation is **linear and hierarchical** — you drill deeper and come back. Like a browser's forward/back history.

```
HOME ──► UPCOMING_MATCHES
          (back button returns to HOME)
```

This is what the current `NavigationStack()` in `MainActivity.kt` implements.

---

### Bottom Navigation (What We're Adding)

Bottom navigation is **parallel and independent** — each tab is its own "world". You're not drilling deeper, you're switching contexts.

```
HOME tab          MATCH RESULTS tab
[HOME]            [MATCH_RESULTS]
  ↕ (back             ↕ (back
   stays in tab)       stays in tab)
[UPCOMING_MATCHES]
```

**Key behavioral difference:**
- Tapping a bottom tab does NOT push to the back stack in the traditional sense
- Each tab preserves its own scroll position and back stack
- The back button inside a tab goes back within that tab
- The back button when you're at a tab's root screen either switches to the Home tab or exits the app

---

## Key Navigation Concepts

### `launchSingleTop`

When tapping a bottom tab you're already on:

```kotlin
navController.navigate(Routes.HOME) {
    launchSingleTop = true  // Don't create a duplicate HOME on the stack
}
```

Without this: tapping "Home" when already on Home creates a second Home screen instance. Bad.

---

### `saveState` and `restoreState`

```kotlin
navController.navigate(Routes.MATCH_RESULTS) {
    saveState = true     // Save HOME tab's scroll position when leaving
    restoreState = true  // Restore MATCH_RESULTS scroll position when returning
}
```

Without this: every time you switch tabs, the tab re-launches from scratch (scroll position lost, API called again).

---

### `popUpTo` for Bottom Nav

```kotlin
navController.navigate(Routes.MATCH_RESULTS) {
    popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}
```

`popUpTo` + `saveState` ensures the back stack doesn't accumulate tab destinations.

**Without `popUpTo`:**
Back stack after switching tabs 3 times: `[HOME, RESULTS, HOME, RESULTS, HOME]`

**With `popUpTo`:**
Back stack: `[HOME]` or `[RESULTS]` — always at most one tab at the root level.

---

### Nested Navigation Graphs (for the future)

Each bottom nav tab can have its own **nested NavGraph** — its own independent back stack.

```kotlin
NavHost(...) {
    navigation(startDestination = Routes.HOME, route = "home_graph") {
        composable(Routes.HOME) { HomeScreen() }
        composable(Routes.UPCOMING_MATCHES) { UpcomingMatchesScreen() }
        // UPCOMING_MATCHES is a stack destination WITHIN the home tab
    }

    navigation(startDestination = Routes.MATCH_RESULTS, route = "results_graph") {
        composable(Routes.MATCH_RESULTS) { MatchResultsScreen() }
    }
}
```

We'll keep it simple for now (flat graph), but this is how production apps (like Cricbuzz) actually structure it.

---

## Architecture of What We're Building

### Current Architecture

```
MainActivity
  └── NavigationStack()
        └── NavHost
              ├── HOME → HomeScreen
              └── UPCOMING_MATCHES → UpcomingMatchesScreen
```

### Target Architecture

```
MainActivity
  └── NavigationStack()
        └── Scaffold (root)
              ├── bottomBar: CricFeedBottomNavBar
              └── NavHost
                    ├── HOME → HomeScreen (has its own TopAppBar)
                    │     └── navigates to UPCOMING_MATCHES
                    ├── MATCH_RESULTS → MatchResultsScreen (has its own TopAppBar)
                    └── UPCOMING_MATCHES → UpcomingMatchesScreen
```

### ViewModel Ownership

Each screen owns its own ViewModel. ViewModels are only shared when two screens genuinely share live state.

```
HomeScreen ──────────────► HomeViewModel
                              ├── homeFeedFlow
                              └── upcomingMatchesFlow

UpcomingMatchesScreen ───────► HomeViewModel  (shared — carousel state is genuinely shared)

MatchResultsScreen ──────────► MatchResultsViewModel  (its own — nothing shared with Home)
                                  └── matchResultFlow
```

`matchResultFlow` moves OUT of `HomeViewModel` and INTO the new `MatchResultsViewModel`. `HomeViewModel` shrinks back to only owning what the home screen and its drill-down (upcoming matches) actually need.

### Key Principle: One Scaffold Owns the BottomBar

The `bottomBar` lives in a **single root Scaffold**. Individual screens (HomeScreen, MatchResultsScreen) only have TopAppBar — NOT their own bottom bar. This avoids:
- Duplicate bottom bars
- Conflicting padding (double insets)
- Screen flickering on tab switch

---

## Step-by-Step Implementation

---

### Step 1: Add the `MATCH_RESULTS` Route

**File:** `presentation/navigation/Routes.kt`

```kotlin
object Routes {
    const val HOME = "home"
    const val UPCOMING_MATCHES = "upcoming_matches"
    const val MATCH_RESULTS = "match_results"   // ← Add this
}
```

**Why:** Every screen needs a unique string key in the NavGraph. This is the identifier the NavController uses to navigate.

---

### Step 2: Create a `BottomNavItem` Sealed Class

**File:** `presentation/navigation/BottomNavItem.kt` ← Create this file

```kotlin
package com.example.cricfeedmobile.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
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
```

**Why a sealed class?**

You could hardcode the bottom nav items directly in the composable, but a sealed class:
- Gives you a **single source of truth** for nav items
- Makes it easy to add a 3rd tab later by just adding a new object
- Keeps route strings, labels, and icons colocated — no drift between files
- Lets you iterate over all items with `listOf(BottomNavItem.Home, BottomNavItem.MatchResults)` to build the nav bar dynamically

---

### Step 3: Create `MatchResultsViewModel`

**File:** `presentation/results/MatchResultsViewModel.kt` ← Create this file

```kotlin
package com.example.cricfeedmobile.presentation.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.cricfeedmobile.domain.model.MatchResult
import com.example.cricfeedmobile.domain.repository.FeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MatchResultsViewModel @Inject constructor(
    private val feedRepository: FeedRepository
) : ViewModel() {

    val matchResultFlow: Flow<PagingData<MatchResult>> = feedRepository
        .getMatchResults()
        .cachedIn(viewModelScope)
}
```

**Why move it here from `HomeViewModel`?**

`matchResultFlow` has zero connection to the home feed or upcoming matches. Keeping it in `HomeViewModel` violates the Single Responsibility Principle — `HomeViewModel` would own three unrelated data concerns. By giving `MatchResultsScreen` its own ViewModel:
- Future features on Match Results (filters, sorting, search) have a clear home
- Testing Match Results logic doesn't require setting up home feed infrastructure
- `HomeViewModel` stays lean and focused on what the home screen actually needs

**Also update `HomeViewModel.kt`** — remove the `matchResultFlow` property that's now in `MatchResultsViewModel`:

```kotlin
// HomeViewModel.kt — remove this line
val matchResultFlow: Flow<PagingData<MatchResult>> = feedRepository
    .getMatchResults()
    .cachedIn(viewModelScope)
```

You can also remove the unused `MatchResult` import from `HomeViewModel` after deleting that line.

---

### Step 4: Create the `MatchResultsScreen`

**File:** `presentation/results/MatchResultsScreen.kt` ← Create this file

The `MatchResultsPagingSource` already exists at `data/paging/MatchResultsPagingSource.kt`. It returns `MatchResult` (the standalone domain model), NOT `FeedItem.MatchResult`.

```kotlin
package com.example.cricfeedmobile.presentation.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.cricfeedmobile.domain.model.MatchResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchResultsScreen(
    viewModel: MatchResultsViewModel = hiltViewModel()
) {
    val matchResults = viewModel.matchResultFlow.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Match Results") })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                items(
                    count = matchResults.itemCount,
                    key = matchResults.itemKey { "${it.matchId}-${it.matchType}" },
                    contentType = { "match_result" }
                ) { index ->
                    matchResults[index]?.let { result ->
                        MatchResultListCard(result = result)
                    }
                }

                item {
                    if (matchResults.loadState.append is LoadState.Loading) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }

            if (matchResults.loadState.refresh is LoadState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            if (matchResults.loadState.refresh is LoadState.Error) {
                Text(
                    text = "Error loading results",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun MatchResultListCard(result: MatchResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = result.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = result.result,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF1976D2),
                modifier = Modifier.padding(top = 4.dp)
            )
            result.playerOfMatch?.let { player ->
                Text(
                    text = "Player of the Match: ${player.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
```

**Why `MatchResultListCard` is separate from `MatchResultCard.kt`?**

`MatchResultCard.kt` takes `FeedItem.MatchResult` — the feed item version with `id`, `timestamp`, and `venue`. The paging source returns `MatchResult` (the standalone domain model), which doesn't have `venue` or `timestamp`. Same data conceptually, but two different types. We create a new simple card rather than force one type into a composable built for another.

---

### Step 5: Create the `CricFeedBottomNavBar` Composable

**File:** `presentation/navigation/CricFeedBottomNavBar.kt` ← Create this file

```kotlin
package com.example.cricfeedmobile.presentation.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun CricFeedBottomNavBar(navController: NavController) {
    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.MatchResults
    )

    // currentBackStackEntryAsState() reactively observes the back stack.
    // Whenever the screen changes, this recomposes automatically.
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        navItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop back to the start destination of the graph,
                        // saving state so the scroll position is remembered.
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid creating multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when re-selecting a previously selected tab
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}
```

**Why `currentBackStackEntryAsState()`?**

The bottom nav bar needs to know which tab is currently active to highlight it. `currentBackStackEntryAsState()` is a `State<NavBackStackEntry?>` — it's reactive. When the user navigates, it emits the new back stack entry, the composable recomposes, and the correct tab becomes highlighted.

Without this, the tab highlight would be static and never update.

---

### Step 6: Refactor `NavigationStack()` in `MainActivity.kt`

**File:** `MainActivity.kt`

This is the most important change. The current `NavigationStack()` has no Scaffold. We need to add one with `bottomBar`.

```kotlin
@Composable
fun NavigationStack() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            // Only show the bottom bar on the top-level tab destinations.
            // We DON'T want the bottom bar to appear on UPCOMING_MATCHES (a drill-down screen).
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val bottomBarRoutes = setOf(Routes.HOME, Routes.MATCH_RESULTS)

            if (currentRoute in bottomBarRoutes) {
                CricFeedBottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    viewModel = hiltViewModel(),
                    navController = navController
                )
            }

            composable(Routes.MATCH_RESULTS) {
                MatchResultsScreen(
                    viewModel = hiltViewModel()
                )
            }

            composable(Routes.UPCOMING_MATCHES) {
                UpcomingMatchesScreen(
                    homeViewModel = hiltViewModel(),
                    onBackClick = { navController.navigateUp() }
                )
            }
        }
    }
}
```

**Why `Modifier.padding(innerPadding)` on `NavHost`?**

The root `Scaffold`'s `innerPadding` reserves space so content is not drawn **behind** the bottom navigation bar. If you forget this, your last list item will be hidden under the bottom nav.

The `Modifier.padding(innerPadding)` is applied to the entire `NavHost`, so ALL screens inside automatically have their content start above the bottom nav.

**Why hide the bottom bar on UPCOMING_MATCHES?**

`UPCOMING_MATCHES` is a **drill-down screen** navigated to from within the Home tab — it's not a tab itself. Showing the bottom nav there would be confusing (which tab is "selected"? Home? It doesn't correspond to either tab). Real apps like Cricbuzz, Twitter, and YouTube hide the bottom nav when drilling into sub-screens.

---

### Step 7: Fix the Scaffold Padding in `HomeScreen`

**File:** `presentation/home/HomeScreen.kt`

Right now `HomeScreen` has its own Scaffold and applies `Modifier.padding(padding)` from that inner Scaffold. The outer Scaffold in `NavigationStack` already gives the NavHost bottom padding for the nav bar.

The inner Scaffold in `HomeScreen` handles the TopAppBar padding — that's fine. BUT we need to make sure the inner Scaffold doesn't accidentally ignore the bottom padding from the outer scaffold.

**The rule**: In Compose, each Scaffold's `innerPadding` is independent. The outer scaffold gives padding to the NavHost (bottom of nav bar). The inner scaffold gives padding for the TopAppBar. They combine correctly as long as the outer padding flows into the NavHost modifier.

Your `HomeScreen` code is fine as-is — no changes needed here. The `Modifier.padding(innerPadding)` on the NavHost in `NavigationStack` will correctly pad the NavHost's available space, and `HomeScreen`'s own Scaffold works within that space.

---

### Step 8: Add the Missing Import in `MainActivity.kt`

You'll need these imports in `MainActivity.kt`:

```kotlin
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue
import com.example.cricfeedmobile.presentation.navigation.CricFeedBottomNavBar
import com.example.cricfeedmobile.presentation.results.MatchResultsScreen
```

---

## How It All Connects

Here's the full data flow for the Match Results tab:

```
User taps "Results" tab
       ↓
CricFeedBottomNavBar.onClick fires
       ↓
navController.navigate(Routes.MATCH_RESULTS) { popUpTo, launchSingleTop, restoreState }
       ↓
NavController updates back stack
       ↓
currentBackStackEntryAsState() emits new entry
       ↓
NavHost switches visible composable to MatchResultsScreen
       ↓
MatchResultsScreen calls hiltViewModel() → MatchResultsViewModel   ← its own VM
       ↓
viewModel.matchResultFlow.collectAsLazyPagingItems()
       ↓
matchResultFlow was created as:
    feedRepository.getMatchResults().cachedIn(viewModelScope)
       ↓
FeedRepositoryImpl.getMatchResults() creates Pager with MatchResultsPagingSource
       ↓
MatchResultsPagingSource.load() calls:
    apiService.getMatchResults(page = 1, limit = 5)
       ↓
GET /api/matches/results?page=1&limit=5
       ↓
Response mapped to List<MatchResult>
       ↓
LoadResult.Page returned → PagingData emitted
       ↓
LazyColumn renders MatchResultListCard for each item
       ↓
User scrolls → prefetch triggers next page load
```

**Note:** `HomeViewModel` is NOT involved here at all. The Match Results screen is fully self-contained through its own ViewModel, its own data flow, and the shared `FeedRepository` interface.

---

## Full File Summary

| File | Action | Why |
|---|---|---|
| `Routes.kt` | Add `MATCH_RESULTS` | New destination needs a route key |
| `BottomNavItem.kt` | Create new | Describes the 2 tabs (icon, label, route) |
| `MatchResultsViewModel.kt` | Create new | Owns match results state, separate from home feed concerns |
| `MatchResultsScreen.kt` | Create new | The new tab screen, uses `MatchResultsViewModel` |
| `CricFeedBottomNavBar.kt` | Create new | The `NavigationBar` composable |
| `MainActivity.kt` | Modify `NavigationStack()` | Add root Scaffold + bottomBar + new route |
| `HomeViewModel.kt` | Remove `matchResultFlow` | That flow now lives in `MatchResultsViewModel` |

**Note:** `FeedRepositoryImpl.kt` and `MatchResultsPagingSource.kt` stay exactly as they are — no data layer changes needed.

---

## Common Mistakes

### Mistake 1: Putting `bottomBar` inside every screen

```kotlin
// ❌ WRONG — HomeScreen has its own Scaffold with a bottomBar
@Composable
fun HomeScreen() {
    Scaffold(
        bottomBar = { CricFeedBottomNavBar(navController) }  // ← Duplicated on every screen!
    ) { ... }
}
```

**Why this is bad**: Every screen creates its own bottom nav instance. They'll flicker when navigating because the bar is destroyed and recreated. They won't share the same `navController` state properly.

**Fix**: Single `Scaffold` at the root `NavigationStack` level owns the bottom bar.

---

### Mistake 2: Navigating to tabs without `launchSingleTop`

```kotlin
// ❌ WRONG
navController.navigate(Routes.HOME)
```

Tapping "Home" when already on Home creates a second Home instance in the back stack. User presses back and sees Home again before exiting.

```kotlin
// ✅ CORRECT
navController.navigate(Routes.HOME) {
    launchSingleTop = true
}
```

---

### Mistake 3: Forgetting `popUpTo` — the tab accumulation bug

```kotlin
// ❌ WRONG — after switching Home → Results → Home → Results
// Back stack: [HOME, RESULTS, HOME, RESULTS]
navController.navigate(Routes.MATCH_RESULTS) {
    launchSingleTop = true
    restoreState = true
}
```

Pressing back goes through every tab switch the user ever made.

```kotlin
// ✅ CORRECT — back stack always stays clean
navController.navigate(Routes.MATCH_RESULTS) {
    popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}
```

---

### Mistake 4: Forgetting `Modifier.padding(innerPadding)` on NavHost

```kotlin
// ❌ WRONG — last item hidden behind the bottom nav bar
NavHost(navController = navController, startDestination = Routes.HOME) {
    ...
}

// ✅ CORRECT
NavHost(
    navController = navController,
    startDestination = Routes.HOME,
    modifier = Modifier.padding(innerPadding)   // ← This
) {
    ...
}
```

---

### Mistake 5: Showing the bottom bar on drill-down screens

```kotlin
// ❌ WRONG — UPCOMING_MATCHES shows the bottom nav bar
bottomBar = { CricFeedBottomNavBar(navController) }

// ✅ CORRECT — only show on top-level tabs
bottomBar = {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    if (currentRoute in setOf(Routes.HOME, Routes.MATCH_RESULTS)) {
        CricFeedBottomNavBar(navController)
    }
}
```

---

### Mistake 6: Reusing another screen's ViewModel out of convenience

```kotlin
// ❌ WRONG — MatchResultsScreen borrows HomeViewModel because it "already has the flow"
@Composable
fun MatchResultsScreen(
    viewModel: HomeViewModel = hiltViewModel()  // ← Wrong owner
) { ... }
```

**Why this is bad**:
- `HomeViewModel` now loads `homeFeedFlow`, `upcomingMatchesFlow`, AND `matchResultFlow` even when the user is only on the Match Results tab — wasted memory and unnecessary paging setup
- Any new Match Results feature (filter, sort, search state) has no clean home — it gets dumped into `HomeViewModel`, which has nothing to do with it
- Unit testing Match Results requires constructing a `HomeViewModel` with all its home feed dependencies

```kotlin
// ✅ CORRECT — dedicated ViewModel with its own scope
@Composable
fun MatchResultsScreen(
    viewModel: MatchResultsViewModel = hiltViewModel()  // ← Right owner
) { ... }
```

**The rule**: Only share a ViewModel between screens when those screens genuinely display and mutate the **same live state**. `HomeScreen` and `UpcomingMatchesScreen` share `HomeViewModel` because the carousel preview state is genuinely shared. `MatchResultsScreen` has nothing to share with `HomeScreen`.

---

## Real-World Parallels

The pattern you're implementing here is used by:

| App | Bottom Nav Tabs | Drill-Down Screens (no bottom nav) |
|---|---|---|
| Cricbuzz | Home, Scores, Series, News, More | Match Detail, Article |
| Twitter/X | Home, Search, Spaces, Notifications, Messages | Tweet thread, Profile |
| YouTube | Home, Shorts, +, Subscriptions, Library | Watch screen, Channel |
| Instagram | Home, Search, Reels, Shop, Profile | Post detail, Story |

The structural pattern is identical to what you're building.

---

**Last Updated**: 2026-02-17
**Status**: Ready to implement — all code included
**Revision**: Added `MatchResultsViewModel` (Step 3) — `matchResultFlow` moved out of `HomeViewModel`