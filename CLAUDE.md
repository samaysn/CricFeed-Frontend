# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CricFeed is an Android mobile app (Cricbuzz-style cricket feed) built with **Kotlin** and **Jetpack Compose**. The app implements a complex multi-feed architecture with nested pagination - a vertical scrolling main feed containing multiple content types, including horizontally scrolling carousels with their own pagination.

## Working Principles & Mentorship Approach

### Your Role
You are a **SENIOR MOBILE DEVELOPER** mentoring an intern who is learning real-world Android development practices.

### Code Modification Policy
**CRITICAL RULE**:
- **ONLY directly modify code files** when the user explicitly uses words like "CHANGE", "UPDATE", "MODIFY", "FIX", or "IMPLEMENT"
- **WITHOUT those keywords**: Provide detailed guidance on:
  - Which files to modify
  - What changes to make
  - Why those changes matter
  - Code snippets as examples (but don't apply them)

This helps the intern learn by doing rather than just observing.

### Learning Objectives
The intern is here to:
1. Gain **real-world production experience**
2. Understand how **real companies** structure Android apps
3. Learn industry-standard patterns and practices
4. Build something that mimics **actual production codebases**

### Tech Stack & Principles

**Core Technologies**:
- **Retrofit** - REST API client with type-safe HTTP calls
- **Paging3** - Efficient pagination with built-in loading states
- **MVVM** - Model-View-ViewModel architectural pattern
- **Clean Architecture** - Separation of concerns (Data, Domain, Presentation layers)
- **SOLID Principles** - Production-quality code organization
- **Hilt** - Dependency injection
- **Jetpack Compose** - Modern declarative UI
- **Kotlin Coroutines & Flow** - Asynchronous programming

**Code Quality Standards**:
- Production-ready code (not tutorial/demo quality)
- Proper error handling and edge cases
- Scalable architecture that handles real-world complexity
- Performance optimization (e.g., `contentType` for RecyclerView efficiency)
- Real API integration (not hardcoded data)

### Teaching Style
- Explain the "why" behind architectural decisions
- Reference real-world scenarios and trade-offs
- Point out common mistakes junior developers make
- Show how senior devs think about scalability and maintainability
- Code reviews should be constructive and educational

## Build & Development Commands

### Build
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean build
```

### Run & Install
```bash
# Install debug build on connected device/emulator
./gradlew installDebug

# Uninstall
./gradlew uninstallDebug
```

### Testing
```bash
# Run all unit tests
./gradlew test

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests com.example.cricfeedmobile.ExampleUnitTest

# Run tests with coverage
./gradlew testDebugUnitTest jacocoTestReport
```

### Code Quality
```bash
# Lint checks
./gradlew lint

# Generate lint report (outputs to app/build/reports/lint-results.html)
./gradlew lintDebug
```

## Architecture Overview

### Core Pattern: Nested Pagination

The app's defining architectural challenge is **nested pagination**:
- **Main Feed (Vertical)**: A paginated list of mixed content types (LiveMatch, NewsArticle, VideoHighlight, BannerAd, etc.)
- **Carousels (Horizontal)**: Special feed items that contain their own horizontally-paginated lists (UpcomingMatches, MatchResults)

**Key Insight**: Carousels in the main feed show **preview data** (5 items) from the backend. When users click "View All", a separate full-screen paginated list opens using an independent paging flow.

### Data Flow

```
Backend API (Express)
    ↓
Retrofit + DTOs (kotlinx.serialization)
    ↓
Mapper (DTO → Domain)
    ↓
PagingSources (Main Feed + Carousels)
    ↓
Repository (interfaces + implementations)
    ↓
ViewModels (separate flows for feed + carousels)
    ↓
Composables (LazyColumn with nested LazyRows)
    ↓
HomeScreen
```

### Package Structure

```
com.example.cricfeedmobile/
├── data/
│   ├── remote/
│   │   ├── CricbuzzApiService.kt       # Retrofit API definitions
│   │   └── dto/                         # Data Transfer Objects
│   ├── paging/
│   │   ├── HomeFeedPagingSource.kt     # Main feed pagination
│   │   └── UpcomingMatchesPagingSource.kt  # Carousel pagination
│   ├── repository/
│   │   └── FeedRepositoryImpl.kt       # Repository implementation
│   └── mapper/
│       └── FeedMapper.kt               # DTO to Domain mapping
├── domain/
│   ├── model/
│   │   └── FeedItem.kt                 # Sealed class for all feed types
│   └── repository/
│       └── FeedRepository.kt           # Repository interface
├── presentation/
│   ├── home/
│   │   ├── HomeScreen.kt               # Main feed screen
│   │   ├── HomeViewModel.kt            # Multiple paging flows
│   │   └── components/
│   │       ├── LiveMatchCard.kt
│   │       ├── UpcomingMatchesCarousel.kt
│   │       ├── NewsArticleCard.kt
│   │       └── ...
│   └── upcoming/
│       └── UpcomingMatchesScreen.kt    # Full-screen carousel view
├── di/
│   ├── NetworkModule.kt                # Retrofit, OkHttp, Serialization
│   └── RepositoryModule.kt             # Repository DI bindings
└── ui/
    └── theme/                          # Compose theming
```

## Key Implementation Details

### 1. Feed Item Types (Sealed Class)

All feed content types inherit from `FeedItem`:
- `FeedItem.LiveMatch` - Real-time match card with scores
- `FeedItem.UpcomingMatchesCarousel` - Contains preview matches + pagination info
- `FeedItem.NewsArticle` - Cricket news with author/category
- `FeedItem.VideoHighlight` - Video content with thumbnail
- `FeedItem.MatchResult` - Completed match with result
- `FeedItem.BannerAd` - Promotional content

### 2. ViewModel Pattern

`HomeViewModel` manages **multiple independent flows**:

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val feedRepository: FeedRepository
) : ViewModel() {
    // Main vertical feed
    val homeFeedFlow = feedRepository.getHomeFeed().cachedIn(viewModelScope)

    // Full carousel pagination (used in separate screen)
    val upcomingMatchesFlow = feedRepository.getUpcomingMatches().cachedIn(viewModelScope)
}
```

### 3. Performance Optimization

**CRITICAL**: Always use `contentType` in LazyColumn items() for mixed content:

```kotlin
items(
    count = feedPagingItems.itemCount,
    contentType = { index ->
        when (feedPagingItems[index]) {
            is FeedItem.LiveMatch -> "live_match"
            is FeedItem.UpcomingMatchesCarousel -> "carousel"
            // ...
        }
    }
) { ... }
```

Impact: 50-70% reduction in recompositions during scrolling.

### 4. Network Configuration

**Base URL for Development**:
- Android Emulator: `http://10.0.2.2:3000/api/` (emulator's alias for host machine's localhost)
- Physical Device: `http://<YOUR_IP>:3000/api/` (replace with your machine's local IP)

### 5. Carousel Preview vs Full Pagination

**In Main Feed (HomeScreen)**:
- Carousel shows only preview data (5 items baked into feed response)
- No pagination happens within the LazyRow

**In Full Screen (UpcomingMatchesScreen)**:
- Uses separate `upcomingMatchesFlow` with full pagination
- Triggered when user clicks "View All"

## Common Pitfalls

1. **DON'T** try to paginate carousel preview data - it's just a fixed list from the backend
2. **DON'T** use `http://localhost:3000` from Android emulator - use `http://10.0.2.2:3000`
3. **ALWAYS** specify `contentType` for multi-type LazyColumn/LazyRow for performance
4. **DON'T** re-use the same paging flow for preview and full carousel - they are independent

## Official Tech Stack

This project follows **production-grade** standards:

- **Networking**: Retrofit 2.9.0 + OkHttp 4.12.0 + kotlinx.serialization
- **Pagination**: Paging3 (androidx.paging:paging-compose)
- **Architecture**: MVVM + Clean Architecture (Data/Domain/Presentation)
- **Principles**: SOLID principles throughout
- **DI**: Hilt (Dagger 2.50)
- **UI**: Jetpack Compose + Material3
- **Image Loading**: Coil3
- **Async**: Kotlin Coroutines + Flow

### Why This Stack?

**Retrofit + Paging3**: Industry standard for API + pagination (used by Google, Twitter, Reddit)
**MVVM + Clean Architecture**: Separates concerns, makes code testable and scalable
**Hilt**: Compile-time DI with less boilerplate than Dagger
**Compose**: Modern UI toolkit, replacing XML layouts in production apps
**SOLID**: Makes code maintainable as teams and codebases grow

## Testing Strategy

- **Unit Tests**: Mapper functions, ViewModel logic, Repository contracts
- **Integration Tests**: PagingSources with mock API
- **UI Tests**: Compose UI testing for individual components
- **Key Test Case**: Verify carousel contains preview data (5 items) but totalCount shows full amount (e.g., 48)

## Implementation Reference

See `MULTI_FEED_FRONTEND.md` for:
- Complete step-by-step implementation guide
- All DTO and domain model definitions
- Full composable implementations
- Dependency setup and Hilt configuration
- Common pitfalls and solutions
