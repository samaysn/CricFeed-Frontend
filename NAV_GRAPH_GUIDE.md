# Navigation Graphs in Jetpack Compose — A Complete Guide

> Written specifically for the CricFeed codebase. Theory first, then exact code you'd write.

---

## Table of Contents

1. [What's Wrong With the Current Approach](#1-whats-wrong-with-the-current-approach)
2. [What is a NavGraph?](#2-what-is-a-navgraph)
3. [Flat vs Nested — The Mental Model](#3-flat-vs-nested--the-mental-model)
4. [Nested Graph Theory Deep Dive](#4-nested-graph-theory-deep-dive)
5. [The Google-Recommended Pattern for Bottom Nav](#5-the-google-recommended-pattern-for-bottom-nav)
6. [How to Refactor CricFeed Step by Step](#6-how-to-refactor-cricfeed-step-by-step)
7. [All the Code](#7-all-the-code)
8. [Common Mistakes](#8-common-mistakes)
9. [How to Add New Screens After Refactoring](#9-how-to-add-new-screens-after-refactoring)

---

## 1. What's Wrong With the Current Approach

Look at your current `MainActivity.kt`:

```kotlin
NavHost(
    navController = navController,
    startDestination = Routes.HOME,
) {
    composable(Routes.HOME) { ... }
    composable(Routes.MATCH_RESULTS) { ... }
    composable(Routes.UPCOMING_MATCHES) { ... }
}
```

And this visibility logic for the bottom bar:

```kotlin
val bottomBarRoutes = setOf(
    Routes.HOME,
    Routes.MATCH_RESULTS,
    Routes.UPCOMING_MATCHES  // ← this one shouldn't be here, but it is
)
if (currentRoute in bottomBarRoutes) {
    CricFeedBottomNavBar(navController = navController)
}
```

**Problems**:

| Problem | Why it hurts |
|---|---|
| All routes are at the same "level" | There's no concept of which screens belong together |
| Manual `bottomBarRoutes` set | Every time you add a screen, you have to remember to update this set. You WILL forget. |
| MainActivity knows too much | It knows about every single screen in the app — this won't scale |
| No route grouping | How do you know `upcoming_matches` is a "detail" screen launched FROM home? The nav graph doesn't express this |
| State loss on tab switch | Without nested graphs, switching between bottom nav tabs doesn't save/restore scroll position and back stack |

In a real production app with 30+ screens, this approach becomes a nightmare.

---

## 2. What is a NavGraph?

A `NavGraph` is a **container of destinations**. Think of it like a folder.

```
NavGraph (Root)
├── composable("home")
├── composable("match_results")
└── composable("upcoming_matches")
```

A **nested** NavGraph is just a NavGraph *inside* another NavGraph:

```
NavGraph (Root)
├── NavGraph ("main_tabs_graph")    ← a sub-folder
│   ├── composable("home")
│   └── composable("match_results")
└── composable("upcoming_matches")  ← still at root level
```

**Key rule**: A NavGraph always has a `startDestination` — the first screen shown when you navigate *to* that graph.

When you `navigate("main_tabs_graph")`, it automatically goes to `"home"` because that's the startDestination of that nested graph.

---

## 3. Flat vs Nested — The Mental Model

### Flat (what you have now)

```
Room (only one room, no walls)
┌────────────────────────────────────────┐
│  home    match_results  upcoming_matches│
└────────────────────────────────────────┘
```

All screens are siblings. Navigation has no understanding of context or ownership.

### Nested (what you want)

```
House (the root NavHost)
├── Living Room (main_tabs_graph — screens with bottom nav)
│   ├── Home tab
│   └── Match Results tab
│
└── Hallway (standalone screens — no bottom nav)
    └── Upcoming Matches (detail screen)
```

Now the nav graph **expresses the structure of your app**. You can glance at it and understand the UX immediately.

---

## 4. Nested Graph Theory Deep Dive

### 4.1 How `navigation {}` works

In Compose Navigation, you create a nested graph with the `navigation` builder:

```kotlin
NavHost(navController, startDestination = "main_tabs_graph") {

    // This is a nested graph
    navigation(
        startDestination = "home",
        route = "main_tabs_graph"
    ) {
        composable("home") { HomeScreen() }
        composable("match_results") { MatchResultsScreen() }
    }

    // This is at the ROOT level (not inside the nested graph)
    composable("upcoming_matches") { UpcomingMatchesScreen() }
}
```

### 4.2 Back Stack Behavior

This is the most important thing to understand. The back stack is a **stack of NavGraph entries**.

When you navigate from Home → Upcoming Matches:

```
Back Stack:
[ main_tabs_graph/home ]  ← bottom
[ upcoming_matches     ]  ← top (currently visible)
```

When you press Back:
```
Back Stack:
[ main_tabs_graph/home ]  ← now visible again
```

The nested graph keeps its **own internal back stack** for tabs. This means:
- If you're on Home tab, navigate within Home to a sub-screen, then switch to Match Results tab...
- When you come back to Home tab, you're still on that sub-screen (not back at the top of Home)

This is the behavior you see in apps like YouTube, Instagram, Twitter.

### 4.3 The `popUpTo` + `saveState` + `restoreState` Pattern

You already use this in `CricFeedBottomNavBar.kt`:

```kotlin
navController.navigate(item.route) {
    popUpTo(navController.graph.findStartDestination().id) {
        saveState = true    // save the back stack of this tab
    }
    launchSingleTop = true  // don't create duplicate
    restoreState = true     // restore saved back stack when returning
}
```

With nested graphs, `findStartDestination()` finds the root of the nested graph (not just any composable), making this work correctly.

### 4.4 Why each bottom nav tab gets its own nested graph

The **Google-recommended** pattern is one nested graph per bottom nav tab:

```kotlin
NavHost(navController, startDestination = "home_graph") {

    navigation(startDestination = "home", route = "home_graph") {
        composable("home") { HomeScreen() }
        // future: composable("home/article_detail/{id}") { ... }
        // future: composable("home/player_profile/{id}") { ... }
    }

    navigation(startDestination = "match_results", route = "match_results_graph") {
        composable("match_results") { MatchResultsScreen() }
        // future: composable("match_results/detail/{id}") { ... }
    }

}
```

**Why?** Because each tab can have its own deep navigation stack. If you add a "Match Detail" screen that opens from Match Results, it lives inside `match_results_graph`. That graph manages its own back stack, completely independent of other tabs.

---

## 5. The Google-Recommended Pattern for Bottom Nav

Reference: [Android Docs — Navigate with Bottom Nav](https://developer.android.com/guide/navigation/navigation-ui#bottom_nav)

The pattern:

1. Each bottom nav tab = one nested graph
2. Bottom nav items point to the **graph route**, not the composable route
3. `findStartDestination()` resolves the actual start screen of that graph
4. `saveState`/`restoreState` preserves tab-specific back stacks

This means `BottomNavItem.Home.route` should be `"home_graph"` (the graph), NOT `"home"` (the screen).

The nav library handles the rest — it knows the startDestination of each graph and navigates there automatically.

---

## 6. How to Refactor CricFeed Step by Step

Here's the plan. **You** implement it; I'm showing you what to do and why.

### Current Structure

```
MainActivity.kt
└── NavigationStack()
    ├── composable(Routes.HOME)
    ├── composable(Routes.MATCH_RESULTS)
    └── composable(Routes.UPCOMING_MATCHES)
```

### Target Structure

```
MainActivity.kt
└── NavigationStack()
    └── NavHost (startDestination = "home_graph")
        │
        ├── navigation(route = "home_graph", startDestination = "home")
        │   └── composable("home")
        │
        ├── navigation(route = "match_results_graph", startDestination = "match_results")
        │   └── composable("match_results")
        │
        └── composable("upcoming_matches")   ← standalone, no bottom nav
```

### Files to Create/Modify

| File | Action | Why |
|---|---|---|
| `Routes.kt` | Modify | Add graph route constants |
| `BottomNavItem.kt` | Modify | Point to graph routes, not screen routes |
| `AppNavGraph.kt` | Create (new file) | Extract NavHost out of MainActivity |
| `MainActivity.kt` | Simplify | Only calls `AppNavGraph()` |
| `CricFeedBottomNavBar.kt` | Tiny fix | Update route matching logic |

### Why extract `AppNavGraph.kt`?

Right now `NavigationStack()` lives in `MainActivity.kt`. That's fine for now, but as you add screens, `MainActivity.kt` becomes a giant file with all your nav logic + screen instantiation.

Real apps extract this into a dedicated `AppNavGraph.kt` (or `NavGraph.kt`) file. `MainActivity.kt` then becomes:

```kotlin
setContent {
    CricFeedMobileTheme {
        AppNavGraph()
    }
}
```

Clean, single-responsibility.

---

## 7. All the Code

Here is the complete implementation. Read this carefully — understand each piece before writing it.

---

### Step 1 — Update `Routes.kt`

Add graph-level route constants. Convention: graph routes end in `_graph`.

```kotlin
object Routes {
    // --- Screen routes (composable destinations) ---
    const val HOME = "home"
    const val UPCOMING_MATCHES = "upcoming_matches"
    const val MATCH_RESULTS = "match_results"

    // --- Graph routes (nested NavGraph containers) ---
    const val HOME_GRAPH = "home_graph"
    const val MATCH_RESULTS_GRAPH = "match_results_graph"
}
```

**Why two levels?**
- `HOME` = the actual screen composable
- `HOME_GRAPH` = the container (nested graph) that holds Home and any future screens that branch off from Home

---

### Step 2 — Update `BottomNavItem.kt`

Bottom nav items now point to **graph routes** — because you're navigating to a *section of the app*, not a single screen.

```kotlin
sealed class BottomNavItem(
    val route: String,        // ← the GRAPH route (e.g. "home_graph")
    val label: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(
        route = Routes.HOME_GRAPH,         // "home_graph", not "home"
        label = "Home",
        icon = Icons.Default.Home
    )

    object MatchResults : BottomNavItem(
        route = Routes.MATCH_RESULTS_GRAPH, // "match_results_graph", not "match_results"
        label = "Results",
        icon = Icons.Default.List
    )
}
```

**Important consequence**: `currentRoute == item.route` in the bottom bar will now be FALSE because the current route is `"home"` (the screen), but `item.route` is `"home_graph"` (the graph).

You need to fix the "selected" detection — see Step 3.

---

### Step 3 — Update `CricFeedBottomNavBar.kt`

The key insight: instead of checking if `currentRoute == item.route`, you need to check if the current destination is **anywhere inside** that item's graph.

Use `hierarchy` for this — it walks up the NavDestination tree.

```kotlin
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavDestination.Companion.hasRoute

@Composable
fun CricFeedBottomNavBar(navController: NavController) {

    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.MatchResults
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        navItems.forEach { item ->
            NavigationBarItem(
                // hierarchy walks the NavDestination chain upward
                // so if you're on "home" inside "home_graph", it finds "home_graph" in the hierarchy
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,

                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
```

**`hierarchy` explained**: If you're on `home` screen inside `home_graph`, the hierarchy looks like:

```
home  →  home_graph  →  root_nav_graph
```

`.any { it.route == "home_graph" }` finds a match, so the Home tab is correctly highlighted. Without `hierarchy`, you'd be comparing `"home"` == `"home_graph"` which is always false.

---

### Step 4 — Create `AppNavGraph.kt`

This is the big one. Create a new file at:
`presentation/navigation/AppNavGraph.kt`

```kotlin
package com.example.cricfeedmobile.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.cricfeedmobile.presentation.home.HomeScreen
import com.example.cricfeedmobile.presentation.matchResults.MatchResultsScreen
import com.example.cricfeedmobile.presentation.upcoming.UpcomingMatchesScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    // Determine which routes should show the bottom bar
    // Now we check GRAPH routes — any screen inside these graphs gets the bottom bar
    val bottomBarGraphs = setOf(
        Routes.HOME_GRAPH,
        Routes.MATCH_RESULTS_GRAPH
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Show bottom bar if we're anywhere inside a bottom-nav graph
    val showBottomBar = currentDestination?.hierarchy?.any { dest ->
        dest.route in bottomBarGraphs
    } == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CricFeedBottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Routes.HOME_GRAPH,   // start at the HOME graph
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {

            // ── Bottom Nav Graph 1: Home Tab ─────────────────────────────
            navigation(
                startDestination = Routes.HOME,
                route = Routes.HOME_GRAPH
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        viewModel = hiltViewModel(),
                        navController = navController
                    )
                }

                // When you add a HomeDetail screen in future:
                // composable("home/article/{articleId}") { ... }
                // composable("home/player/{playerId}") { ... }
            }

            // ── Bottom Nav Graph 2: Match Results Tab ────────────────────
            navigation(
                startDestination = Routes.MATCH_RESULTS,
                route = Routes.MATCH_RESULTS_GRAPH
            ) {
                composable(Routes.MATCH_RESULTS) {
                    MatchResultsScreen(
                        viewModel = hiltViewModel()
                    )
                }

                // When you add MatchDetail in future:
                // composable("match_results/detail/{matchId}") { ... }
            }

            // ── Standalone Screen: No Bottom Nav ─────────────────────────
            // upcoming_matches is NOT inside any graph — so showBottomBar is false here
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

---

### Step 5 — Simplify `MainActivity.kt`

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CricFeedMobileTheme {
                AppNavGraph()
            }
        }
    }
}
```

That's it. `MainActivity` doesn't know about any screens. It just launches the nav graph.

---

## 8. Common Mistakes

### Mistake 1: Pointing BottomNavItem to the screen route instead of the graph route

```kotlin
// WRONG
object Home : BottomNavItem(route = Routes.HOME, ...)

// CORRECT
object Home : BottomNavItem(route = Routes.HOME_GRAPH, ...)
```

If you use the screen route, `saveState`/`restoreState` will work incorrectly and tab back stacks won't be preserved.

### Mistake 2: Using `currentRoute == item.route` for selected state

```kotlin
// WRONG — always false because "home" != "home_graph"
selected = currentRoute == item.route

// CORRECT — hierarchy walks up through nested graphs
selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
```

### Mistake 3: Forgetting `import androidx.navigation.NavDestination.Companion.hierarchy`

`hierarchy` is an extension property that requires an import. If you use it without importing, it won't compile.

### Mistake 4: Mixing graph and screen routes in `bottomBarRoutes`

```kotlin
// WRONG — mixing levels
val bottomBarRoutes = setOf(Routes.HOME, Routes.HOME_GRAPH, ...)

// CORRECT — only graph routes
val bottomBarGraphs = setOf(Routes.HOME_GRAPH, Routes.MATCH_RESULTS_GRAPH)
```

### Mistake 5: Putting detail screens INSIDE the wrong graph

If `MatchDetail` should only be reachable from Match Results (and should NOT show the bottom bar), put it inside `match_results_graph`.

If it should be accessible from *multiple* places and shouldn't show the bottom bar, put it at the root level (outside all nested graphs).

---

## 9. How to Add New Screens After Refactoring

### Case A: New bottom nav tab (e.g. "Rankings")

1. Add `const val RANKINGS = "rankings"` and `const val RANKINGS_GRAPH = "rankings_graph"` to `Routes.kt`
2. Add `object Rankings : BottomNavItem(route = Routes.RANKINGS_GRAPH, ...)` to `BottomNavItem.kt`
3. Add `Rankings` to the `navItems` list in `CricFeedBottomNavBar.kt`
4. Add `Rankings.RANKINGS_GRAPH` to `bottomBarGraphs` in `AppNavGraph.kt`
5. Add the nested graph block to `AppNavGraph.kt`:
   ```kotlin
   navigation(startDestination = Routes.RANKINGS, route = Routes.RANKINGS_GRAPH) {
       composable(Routes.RANKINGS) { RankingsScreen() }
   }
   ```

That's it. 5 mechanical steps. Nothing else to touch.

### Case B: New detail screen inside an existing tab (e.g. "Article Detail" from Home)

1. Add `const val ARTICLE_DETAIL = "home/article/{articleId}"` to `Routes.kt`
2. Add inside `home_graph` in `AppNavGraph.kt`:
   ```kotlin
   composable(Routes.ARTICLE_DETAIL) { backStackEntry ->
       val articleId = backStackEntry.arguments?.getString("articleId")
       ArticleDetailScreen(articleId = articleId)
   }
   ```
3. Navigate from HomeScreen: `navController.navigate("home/article/$id")`

The bottom bar stays visible because you're still inside `home_graph`.

### Case C: New standalone/modal screen (no bottom bar)

1. Add route to `Routes.kt`
2. Add `composable` at the **root level** of `NavHost` in `AppNavGraph.kt` (NOT inside any `navigation {}` block)
3. The bottom bar automatically hides because this destination isn't inside any `bottomBarGraphs` graph

---

## Visual Summary

```
AppNavGraph.kt (the single source of truth)
│
└── NavHost(startDestination = HOME_GRAPH)
    │
    ├── navigation(route = HOME_GRAPH)        ← Bottom bar visible
    │   ├── composable(HOME)                  ← HomeScreen
    │   └── composable(ARTICLE_DETAIL)        ← future (bottom bar still visible)
    │
    ├── navigation(route = MATCH_RESULTS_GRAPH) ← Bottom bar visible
    │   ├── composable(MATCH_RESULTS)           ← MatchResultsScreen
    │   └── composable(MATCH_DETAIL)            ← future (bottom bar still visible)
    │
    └── composable(UPCOMING_MATCHES)            ← Bottom bar HIDDEN (standalone)
```

The bottom bar logic is now **zero-maintenance** — you never manually maintain a list of routes. The graph structure itself encodes which screens have the bottom bar.