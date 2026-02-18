# Jetpack Navigation 3 — CricFeed Implementation Guide

> Navigation 3 (`androidx.navigation3`) is a brand-new library announced at Google I/O 2025.
> It is **not** an update to `androidx.navigation` — it is a separate library with a completely
> different programming model built from the ground up for Compose.

---

## Table of Contents

1. [Why Navigation 3 Exists](#1-why-navigation-3-exists)
2. [The Core Mental Model Shift](#2-the-core-mental-model-shift)
3. [Nav2 vs Nav3 — Side-by-Side Comparison](#3-nav2-vs-nav3--side-by-side-comparison)
4. [The Four Core Concepts](#4-the-four-core-concepts)
5. [Dependency Setup](#5-dependency-setup)
6. [Step-by-Step CricFeed Implementation](#6-step-by-step-cricfeed-implementation)
7. [Arguments — The Right Way](#7-arguments--the-right-way)
8. [Bottom Navigation Bar in Nav3](#8-bottom-navigation-bar-in-nav3)
9. [ViewModel Integration](#9-viewmodel-integration)
10. [Back Stack Control](#10-back-stack-control)
11. [Hiding Bottom Bar on Detail Screens](#11-hiding-bottom-bar-on-detail-screens)
12. [Common Mistakes](#12-common-mistakes)
13. [Full CricFeed Code](#13-full-cricfeed-code)

---

## 1. Why Navigation 3 Exists

Nav2 had three fundamental design flaws that couldn't be fixed with incremental updates:

### Problem 1 — Back Stack is Hidden State

In Nav2, the back stack lives *inside* NavController. You cannot directly observe or manipulate it as Compose state:

```kotlin
// Nav2: back stack is opaque — you work around it with callbacks
navController.navigate("screen")
navController.popBackStack()
navController.navigate("screen") {
    popUpTo("home") { inclusive = true }  // ← string-based, fragile
}
```

In Compose, this is wrong. **State should be visible**. The back stack IS navigation state — it should be a Compose state object you can read, modify, and observe directly.

### Problem 2 — Single Pane Only

`NavHost` in Nav2 only ever shows ONE destination at a time (the top of the back stack). This makes adaptive layouts (tablet list-detail, foldables with two panes) nearly impossible without giant hacks.

### Problem 3 — String Routes Are Not Type-Safe

```kotlin
// Nav2: spell it wrong → runtime crash, not compile error
navController.navigate("upcoming_matches/${matchId}")

// Then on the receiving end, manually parse it
val id = backStackEntry.arguments?.getString("matchId")  // nullable, error-prone
```

Nav3 fixes all three.

---

## 2. The Core Mental Model Shift

| Concept | Nav2 | Nav3 |
|---|---|---|
| Where is navigation state? | Inside NavController (opaque) | In your composable as `rememberNavBackStack()` |
| What is a route? | A `String` | A `@Serializable` Kotlin class/object |
| What renders screens? | `NavHost {}` | `NavDisplay()` |
| How do you navigate? | `navController.navigate("string")` | `backStack.add(RouteObject)` |
| How do you go back? | `navController.navigateUp()` | `backStack.removeLastOrNull()` |
| Where are screens defined? | `composable("route") {}` blocks | `entryProvider { entry<Route> {} }` |
| Arguments | String path params + `arguments?.getString()` | Fields on data class (`data class Detail(val id: String)`) |

**The key insight**: In Nav3, the back stack is just a `SnapshotStateList<NavKey>`. It's real Compose state. You `.add()` to navigate, you `.removeLast()` to go back. That's it.

---

## 3. Nav2 vs Nav3 — Side-by-Side Comparison

### Routes

```kotlin
// ── NAV 2 ─────────────────────────────────────────────────────────────────
object Routes {
    const val HOME = "home"
    const val UPCOMING_MATCHES = "upcoming_matches"
    const val MATCH_RESULTS = "match_results"
}

// ── NAV 3 ─────────────────────────────────────────────────────────────────
// No strings. No object with consts. Just @Serializable types.
@Serializable data object Home : NavKey
@Serializable data object MatchResults : NavKey
@Serializable data object UpcomingMatches : NavKey
```

### Setup

```kotlin
// ── NAV 2 ─────────────────────────────────────────────────────────────────
val navController = rememberNavController()
NavHost(navController = navController, startDestination = Routes.HOME) {
    composable(Routes.HOME) { HomeScreen() }
    composable(Routes.MATCH_RESULTS) { MatchResultsScreen() }
}

// ── NAV 3 ─────────────────────────────────────────────────────────────────
val backStack = rememberNavBackStack(Home)   // Home is the start destination
NavDisplay(
    backStack = backStack,
    entryProvider = entryProvider {
        entry<Home> { HomeScreen(backStack = backStack) }
        entry<MatchResults> { MatchResultsScreen(backStack = backStack) }
    }
)
```

### Navigating

```kotlin
// ── NAV 2 ─────────────────────────────────────────────────────────────────
navController.navigate(Routes.UPCOMING_MATCHES)

// ── NAV 3 ─────────────────────────────────────────────────────────────────
backStack.add(UpcomingMatches)   // just add to the list
```

### Going Back

```kotlin
// ── NAV 2 ─────────────────────────────────────────────────────────────────
navController.navigateUp()

// ── NAV 3 ─────────────────────────────────────────────────────────────────
backStack.removeLastOrNull()
```

---

## 4. The Four Core Concepts

### 4.1 — `NavKey`

Every screen in Nav3 is a `NavKey`. A `NavKey` is any `@Serializable` class/object that acts as the identity of a destination.

```kotlin
// Screens with NO arguments → data object
@Serializable data object Home : NavKey
@Serializable data object MatchResults : NavKey

// Screens WITH arguments → data class (fields ARE the arguments)
@Serializable data class MatchDetail(val matchId: String) : NavKey
@Serializable data class ArticleDetail(val articleId: Int, val title: String) : NavKey
```

**Why `@Serializable`?** Nav3 uses kotlinx.serialization internally to handle deep links and state restoration. You already have the serialization plugin in CricFeed's `build.gradle.kts`.

### 4.2 — `rememberNavBackStack`

This is the entire navigation state. It's a `SnapshotStateList<NavKey>`:

```kotlin
// Start with Home on the stack
val backStack = rememberNavBackStack(Home)

// backStack is now: [Home]

// After navigating to MatchResults:
backStack.add(MatchResults)
// backStack is now: [Home, MatchResults]

// After going back:
backStack.removeLastOrNull()
// backStack is now: [Home]
```

Because it's `SnapshotStateList`, Compose automatically recomposes whenever it changes. Nav3 reacts to the list; you just modify the list.

### 4.3 — `NavDisplay`

The component that renders whichever `NavKey` is at the top of the back stack.

```kotlin
NavDisplay(
    backStack = backStack,
    entryProvider = entryProvider {
        entry<Home> { /* composable content */ }
        entry<MatchResults> { /* composable content */ }
    }
)
```

`NavDisplay` watches `backStack`, takes the last entry, finds the matching `entry<T>` block, and renders it.

### 4.4 — `entryProvider`

Replaces `composable {}` blocks from Nav2. Maps each `NavKey` type to its composable content.

```kotlin
entryProvider {
    entry<Home> {
        HomeScreen(
            onNavigateToResults = { backStack.add(MatchResults) }
        )
    }
    entry<MatchDetail> { navEntry ->
        // navEntry.key is the typed NavKey — no argument parsing needed
        val key = navEntry.key  // type: MatchDetail
        MatchDetailScreen(matchId = key.matchId)
    }
}
```

---

## 5. Dependency Setup

### Remove Nav2

In `app/build.gradle.kts`, **remove** these lines:
```kotlin
// DELETE these Nav2 dependencies:
val nav_version = "2.9.7"
implementation("androidx.navigation:navigation-compose:$nav_version")
implementation("androidx.navigation:navigation-fragment:$nav_version")
implementation("androidx.navigation:navigation-ui:$nav_version")
implementation("androidx.navigation:navigation-dynamic-features-fragment:$nav_version")
androidTestImplementation("androidx.navigation:navigation-testing:$nav_version")
```

### Add Nav3

```kotlin
// Navigation 3 — core
implementation("androidx.navigation3:navigation3-runtime:1.0.0")
implementation("androidx.navigation3:navigation3-ui:1.0.0")

// ViewModel integration for Nav3
implementation("androidx.lifecycle:lifecycle-viewmodel-navigation3:2.10.0")

// Hilt + Nav3 ViewModel scoping (replaces hilt-navigation-compose for Nav2)
implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
```

> **Note on `hilt-navigation-compose`**: Keep this for `hiltViewModel()`. With Nav3, you scope ViewModels
> to the `NavEntry` using `navEntryViewModel()` from `lifecycle-viewmodel-navigation3`.

---

## 6. Step-by-Step CricFeed Implementation

### Step 1 — Delete `Routes.kt`

This file becomes irrelevant. Nav3 doesn't use string routes. Delete:
`presentation/navigation/Routes.kt`

### Step 2 — Create `AppRoutes.kt`

Create at: `presentation/navigation/AppRoutes.kt`

```kotlin
package com.example.cricfeedmobile.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// ── Bottom Nav Destinations ────────────────────────────────────────────────
@Serializable data object Home : NavKey
@Serializable data object MatchResults : NavKey

// ── Standalone / Detail Destinations ──────────────────────────────────────
// No args needed for now — stays a data object
@Serializable data object UpcomingMatches : NavKey

// Future destinations (add fields when you need arguments):
// @Serializable data class ArticleDetail(val articleId: Int) : NavKey
// @Serializable data class MatchDetail(val matchId: String) : NavKey
```

**Why this structure?**
- `Home`, `MatchResults` → bottom nav tabs. No arguments.
- `UpcomingMatches` → pushed on top of `Home`, hides bottom bar. No arguments yet.
- Future detail screens get their arguments as data class fields.

### Step 3 — Define Bottom Nav Items

Update `BottomNavItem.kt` — instead of a `route: String`, the item now holds a reference to its `NavKey`:

```kotlin
package com.example.cricfeedmobile.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey

sealed class BottomNavItem(
    val key: NavKey,           // ← NavKey instead of route String
    val label: String,
    val icon: ImageVector
) {
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
```

### Step 4 — Update `CricFeedBottomNavBar.kt`

Nav3 gives you direct back stack access — use the last entry to determine which tab is selected:

```kotlin
package com.example.cricfeedmobile.presentation.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey

@Composable
fun CricFeedBottomNavBar(
    currentKey: NavKey,                          // the top-of-stack NavKey
    onTabSelected: (NavKey) -> Unit              // callback to change tab
) {
    val navItems = listOf(
        BottomNavItem.HomeTab,
        BottomNavItem.MatchResultsTab
    )

    NavigationBar {
        navItems.forEach { item ->
            NavigationBarItem(
                // Simple equality check — no hierarchy() needed
                selected = currentKey == item.key,
                onClick = { onTabSelected(item.key) },
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

**Notice what's gone**: No `NavController`, no `hierarchy`, no `popUpTo`, no `saveState`/`restoreState`. You just check `currentKey == item.key`.

### Step 5 — Create `AppNavGraph.kt`

This replaces the `NavigationStack()` composable in `MainActivity.kt`. Create at:
`presentation/navigation/AppNavGraph.kt`

```kotlin
package com.example.cricfeedmobile.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.entryProvider
import com.example.cricfeedmobile.presentation.home.HomeScreen
import com.example.cricfeedmobile.presentation.matchResults.MatchResultsScreen
import com.example.cricfeedmobile.presentation.upcoming.UpcomingMatchesScreen

// Which NavKeys show the bottom bar
private val bottomNavKeys: Set<NavKey> = setOf(Home, MatchResults)

@Composable
fun AppNavGraph() {
    val backStack = rememberNavBackStack(Home)   // start at Home
    val currentKey = backStack.lastOrNull()      // what's currently on screen

    // Bottom bar is shown only when the top-of-stack is a bottom nav destination
    val showBottomBar = currentKey in bottomNavKeys

    Scaffold(
        bottomBar = {
            if (showBottomBar && currentKey != null) {
                CricFeedBottomNavBar(
                    currentKey = currentKey,
                    onTabSelected = { selectedKey ->
                        // Navigate to a tab — avoid duplicates and pop correctly
                        if (currentKey != selectedKey) {
                            // Remove all items above the bottom-nav level
                            // and navigate to the selected tab
                            val existingIndex = backStack.indexOfLast { it == selectedKey }
                            if (existingIndex >= 0) {
                                // Tab already visited — pop to it (restores state)
                                backStack.removeRange(existingIndex + 1, backStack.size)
                            } else {
                                // First time visiting — keep Home, add new tab
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
```

**What makes this work**:
- `showBottomBar = currentKey in bottomNavKeys` — zero-maintenance. If you add a new tab, add it to `bottomNavKeys`. If you add a detail screen, don't add it — it automatically hides the bar.
- `backStack.removeLastOrNull()` is the entire "go back" implementation.
- No `NavController` is passed anywhere. Screens either get a `backStack` reference directly, or use lambda callbacks (shown above).

### Step 6 — Simplify `MainActivity.kt`

```kotlin
package com.example.cricfeedmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.cricfeedmobile.presentation.navigation.AppNavGraph
import com.example.cricfeedmobile.ui.theme.CricFeedMobileTheme
import dagger.hilt.android.AndroidEntryPoint

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

`MainActivity` is now 15 lines. It has no idea what screens exist. Clean.

---

## 7. Arguments — The Right Way

This is where Nav3 genuinely shines. Arguments are just fields on your `NavKey`.

### Nav2 (messy)

```kotlin
// Define a string route with a placeholder
const val MATCH_DETAIL = "match_detail/{matchId}"

// Navigate — string interpolation, nullable at runtime
navController.navigate("match_detail/$matchId")

// Receive — manual parsing, null checks required
composable(
    route = Routes.MATCH_DETAIL,
    arguments = listOf(navArgument("matchId") { type = NavType.StringType })
) { backStackEntry ->
    val matchId = backStackEntry.arguments?.getString("matchId") ?: return@composable
    MatchDetailScreen(matchId = matchId)
}
```

### Nav3 (clean)

```kotlin
// 1. Define the key — fields ARE the arguments
@Serializable data class MatchDetail(val matchId: String) : NavKey

// 2. Navigate — compile-time safe
backStack.add(MatchDetail(matchId = "match_123"))

// 3. Receive — no parsing, no nulls, fully typed
entry<MatchDetail> { navEntry ->
    val key = navEntry.key          // type: MatchDetail, not nullable
    MatchDetailScreen(matchId = key.matchId)
}
```

**You cannot navigate to `MatchDetail` without providing `matchId` — the compiler enforces it.**

### Multiple Arguments

```kotlin
// Nav2: "article/{categoryId}/{articleId}/{title}" — error-prone
// Nav3:
@Serializable
data class ArticleDetail(
    val categoryId: Int,
    val articleId: String,
    val title: String           // can pass full strings, no encoding needed
) : NavKey

// Navigate:
backStack.add(ArticleDetail(categoryId = 2, articleId = "abc", title = "India vs AUS"))

// Receive:
entry<ArticleDetail> { navEntry ->
    val key = navEntry.key
    ArticleDetailScreen(
        categoryId = key.categoryId,
        articleId = key.articleId,
        title = key.title
    )
}
```

---

## 8. Bottom Navigation Bar in Nav3

Nav3 doesn't have a built-in "tabs with back stack preservation" like Nav2's `saveState`/`restoreState`. You manage tab back stacks yourself. Here are two common patterns.

### Pattern A — Simple Tab Switching (Used in CricFeed above)

Keep a flat back stack. When switching tabs, pop to the previous visit of that tab, or just add it:

```kotlin
onTabSelected = { selectedKey ->
    if (currentKey != selectedKey) {
        val existingIndex = backStack.indexOfLast { it == selectedKey }
        if (existingIndex >= 0) {
            backStack.removeRange(existingIndex + 1, backStack.size)
        } else {
            backStack.add(selectedKey)
        }
    }
}
```

**Good for**: Simple apps where tabs don't have deep navigation inside them.

### Pattern B — Per-Tab Back Stacks (YouTube/Instagram style)

Each tab maintains its own independent back stack. Switching tabs switches the entire stack:

```kotlin
@Composable
fun AppNavGraph() {
    // A back stack per tab
    val homeStack = remember { mutableStateListOf<NavKey>(Home) }
    val resultsStack = remember { mutableStateListOf<NavKey>(MatchResults) }

    var activeTab by remember { mutableStateOf<NavKey>(Home) }

    // The active stack is whichever tab is selected
    val activeBackStack = when (activeTab) {
        is Home -> homeStack
        is MatchResults -> resultsStack
        else -> homeStack
    }

    Scaffold(
        bottomBar = {
            CricFeedBottomNavBar(
                currentKey = activeTab,
                onTabSelected = { selectedKey ->
                    activeTab = selectedKey
                    // The NavDisplay switches to the new stack automatically
                }
            )
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = activeBackStack,   // ← swap the whole stack on tab change
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            onBack = {
                if (activeBackStack.size > 1) {
                    activeBackStack.removeLastOrNull()
                }
                // else: minimize app (system handles it)
            },
            entryProvider = entryProvider {
                entry<Home> {
                    HomeScreen(
                        onNavigateToUpcoming = { homeStack.add(UpcomingMatches) }
                    )
                }
                entry<MatchResults> { MatchResultsScreen() }
                entry<UpcomingMatches> {
                    UpcomingMatchesScreen(
                        onBackClick = { homeStack.removeLastOrNull() }
                    )
                }
            }
        )
    }
}
```

**Good for**: Apps where each tab has its own navigation sub-flow (like YouTube: Home tab can go Home → Video → Channel → Video…).

---

## 9. ViewModel Integration

### Standard `hiltViewModel()` (still works)

For screen-level ViewModels that don't need to be scoped to the nav entry, `hiltViewModel()` works exactly as before:

```kotlin
entry<Home> {
    HomeScreen(viewModel = hiltViewModel())
}
```

### Nav Entry-scoped ViewModel (new in Nav3)

For ViewModels that should be scoped to a specific back stack entry (survive configuration changes but be cleared when that entry is removed from the back stack), use `navEntryViewModel`:

```kotlin
import androidx.lifecycle.viewmodel.navigation3.navEntryViewModel

entry<MatchDetail> { navEntry ->
    val viewModel: MatchDetailViewModel = navEntryViewModel(
        navEntry = navEntry,
        viewModelStoreOwner = /* activity or appropriate owner */
    )
    MatchDetailScreen(viewModel = viewModel)
}
```

> **For CricFeed right now**: `hiltViewModel()` is sufficient. `navEntryViewModel` becomes
> important when you have two instances of the same screen on the stack (e.g., two different
> match details open in the back stack) and need them to have separate ViewModel instances.

---

## 10. Back Stack Control

Direct back stack manipulation replaces all Nav2's `popUpTo` patterns:

### Navigate and clear history (like a login → home flow)

```kotlin
// Nav2:
navController.navigate(Routes.HOME) {
    popUpTo(0) { inclusive = true }
}

// Nav3:
backStack.clear()
backStack.add(Home)
```

### Pop to a specific destination

```kotlin
// Nav2:
navController.popBackStack(Routes.HOME, inclusive = false)

// Nav3:
val homeIndex = backStack.indexOfFirst { it is Home }
if (homeIndex >= 0) {
    backStack.removeRange(homeIndex + 1, backStack.size)
}
```

### Single top (don't add duplicate)

```kotlin
// Nav2:
navController.navigate(route) { launchSingleTop = true }

// Nav3:
if (backStack.none { it == MatchResults }) {
    backStack.add(MatchResults)
}
```

---

## 11. Hiding Bottom Bar on Detail Screens

In Nav2 you maintained a `bottomBarRoutes` set. In Nav3 it's even simpler:

```kotlin
// The set of NavKeys that get a bottom bar
private val bottomNavKeys: Set<NavKey> = setOf(Home, MatchResults)

val showBottomBar = backStack.lastOrNull() in bottomNavKeys
```

When `UpcomingMatches` is pushed onto the stack, `lastOrNull()` returns `UpcomingMatches` which is NOT in `bottomNavKeys`, so the bar hides. When you go back and `Home` is on top again, the bar reappears. Automatic.

---

## 12. Common Mistakes

### Mistake 1: Forgetting `@Serializable` on NavKeys

```kotlin
// WRONG — will crash at runtime
data object Home : NavKey

// CORRECT
@Serializable data object Home : NavKey
```

Nav3 uses kotlinx.serialization to create the NavKey identifiers. Missing the annotation causes a runtime crash, not a compile error.

### Mistake 2: Using `NavController` — it doesn't exist in Nav3

```kotlin
// WRONG — NavController is a Nav2 concept
val navController = rememberNavController()

// CORRECT — Nav3 uses back stack
val backStack = rememberNavBackStack(Home)
```

### Mistake 3: Passing `backStack` directly to composables creates tight coupling

```kotlin
// WORKS but tightly couples your screen to navigation
entry<Home> {
    HomeScreen(backStack = backStack)  // screen now knows about navigation
}

// BETTER — pass only what the screen needs
entry<Home> {
    HomeScreen(
        onViewAll = { backStack.add(UpcomingMatches) },
        onMatchTap = { id -> backStack.add(MatchDetail(matchId = id)) }
    )
}
```

Lambda callbacks keep your screens unaware of the nav library. Easier to test and reuse.

### Mistake 4: Using string-based deep links from Nav2

Deep linking in Nav3 works differently. The `@Serializable` routes handle serialization, but deep link URI mapping is configured separately. Don't mix Nav2 deep link patterns.

### Mistake 5: Forgetting `onBack` in `NavDisplay`

```kotlin
// WRONG — system back button won't work
NavDisplay(backStack = backStack, entryProvider = entryProvider { ... })

// CORRECT
NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider = entryProvider { ... }
)
```

---

## 13. Full CricFeed Code

### File: `presentation/navigation/AppRoutes.kt`

```kotlin
package com.example.cricfeedmobile.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Home : NavKey
@Serializable data object MatchResults : NavKey
@Serializable data object UpcomingMatches : NavKey
```

### File: `presentation/navigation/BottomNavItem.kt`

```kotlin
package com.example.cricfeedmobile.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey

sealed class BottomNavItem(
    val key: NavKey,
    val label: String,
    val icon: ImageVector
) {
    object HomeTab : BottomNavItem(Home, "Home", Icons.Default.Home)
    object MatchResultsTab : BottomNavItem(MatchResults, "Results", Icons.Default.List)
}
```

### File: `presentation/navigation/CricFeedBottomNavBar.kt`

```kotlin
package com.example.cricfeedmobile.presentation.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey

@Composable
fun CricFeedBottomNavBar(
    currentKey: NavKey,
    onTabSelected: (NavKey) -> Unit
) {
    val navItems = listOf(BottomNavItem.HomeTab, BottomNavItem.MatchResultsTab)

    NavigationBar {
        navItems.forEach { item ->
            NavigationBarItem(
                selected = currentKey == item.key,
                onClick = { onTabSelected(item.key) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
```

### File: `presentation/navigation/AppNavGraph.kt`

```kotlin
package com.example.cricfeedmobile.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.entryProvider
import com.example.cricfeedmobile.presentation.home.HomeScreen
import com.example.cricfeedmobile.presentation.matchResults.MatchResultsScreen
import com.example.cricfeedmobile.presentation.upcoming.UpcomingMatchesScreen

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
                                backStack.removeRange(existingIndex + 1, backStack.size)
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
                entry<Home> {
                    HomeScreen(
                        viewModel = hiltViewModel(),
                        onNavigateToUpcoming = { backStack.add(UpcomingMatches) }
                    )
                }
                entry<MatchResults> {
                    MatchResultsScreen(viewModel = hiltViewModel())
                }
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
```

### File: `MainActivity.kt`

```kotlin
package com.example.cricfeedmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.cricfeedmobile.presentation.navigation.AppNavGraph
import com.example.cricfeedmobile.ui.theme.CricFeedMobileTheme
import dagger.hilt.android.AndroidEntryPoint

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

---

## Visual Summary

```
Nav2 Mental Model:
  navController.navigate("string") → NavHost reads string → finds composable("string") block

Nav3 Mental Model:
  backStack.add(RouteObject) → NavDisplay reads backStack.last() → finds entry<RouteType> block

Nav3 Back Stack for CricFeed:

  Start:              [Home]
  Switch to Results:  [Home, MatchResults]
  Switch back Home:   [Home]              ← indexOfLast found Home, removed above
  View All click:     [Home, UpcomingMatches]  ← bottom bar hides
  Back:               [Home]              ← bottom bar shows again
```

---

## Known Current Limitations of Nav3 (as of 2025)

| Feature | Status |
|---|---|
| Deep links from notifications/URLs | Not fully supported yet |
| Passing results back from a screen | No built-in API (use shared ViewModel or callbacks) |
| Nav3 + Hilt `navEntryViewModel` | Works, but less documented |
| Transition animations | Supported via `entryDecorators` |
| Testing utilities | Less mature than Nav2 |

These are expected to be addressed in upcoming Nav3 releases. For CricFeed, none of them are blockers.

---

Sources:
- [Announcing Jetpack Navigation 3](https://android-developers.googleblog.com/2025/05/announcing-jetpack-navigation-3-for-compose.html)
- [Migrate from Navigation 2 to Navigation 3](https://developer.android.com/guide/navigation/navigation-3/migration-guide)
- [navigation3 Release Notes](https://developer.android.com/jetpack/androidx/releases/navigation3)