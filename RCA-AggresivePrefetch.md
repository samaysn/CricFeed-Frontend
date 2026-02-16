# RCA: Aggressive Prefetching in Home Feed

## Symptom
At initial load, the app made **7 consecutive API calls** instead of 1-2 after that, works as intended:
```
GET /api/feed/home?page=1&limit=18 200 2.280 ms
GET /api/feed/home?page=2&limit=18 200 1.267 ms
GET /api/feed/home?page=3&limit=18 200 1.157 ms
GET /api/feed/home?page=4&limit=18 200 0.881 ms
GET /api/feed/home?page=5&limit=18 200 0.932 ms
GET /api/feed/home?page=6&limit=18 200 0.862 ms
GET /api/feed/home?page=7&limit=18 200 0.776 ms
```

## Root Cause
**Incorrect key extraction method in `HomeFeedList.kt`:**

```kotlin
// ❌ WRONG - Triggers aggressive prefetching
key = { index -> items[index]?.id ?: index }
```

**Why this caused the issue:**
- Accessing `items[index]` directly signals to Paging3 that those indices are being "observed"
- LazyColumn calls the `key` lambda multiple times during composition and layout calculations
- Each `items[index]` access is interpreted by Paging3 as "this item is about to be displayed"
- This triggers the prefetch mechanism prematurely, loading pages 1-7 immediately

## Deep Dive: How Keys Work

### ❌ Old Approach: `key = { index -> items[index]?.id ?: index }`

**What happens during initial composition:**

```
1. LazyColumn measures layout
   → "I need to display items 0-10 on screen"

2. For each visible item, LazyColumn calls key lambda:
   → key(0) → items[0]?.id → Paging3: "🔔 Item 0 accessed!"
   → key(1) → items[1]?.id → Paging3: "🔔 Item 1 accessed!"
   → key(2) → items[2]?.id → Paging3: "🔔 Item 2 accessed!"
   ...
   → key(10) → items[10]?.id → Paging3: "🔔 Item 10 accessed!"

3. LazyColumn remeasures during layout passes (happens multiple times)
   → key(0-10) called again
   → items[0-10] accessed again
   → Paging3: "🔔 Multiple accesses to items 0-10!"

4. Paging3's ItemSnapshotList tracks these accesses:
   → "Items 0-10 were accessed multiple times"
   → "User must be scrolling fast or needs more data"
   → Triggers prefetch: Load page 2

5. Now items 0-35 exist, layout recalculates:
   → key() called for more items to determine layout
   → items[11-35] accessed during measurement
   → Paging3: "🔔 Items 11-35 accessed!"
   → Triggers prefetch: Load page 3

6. Cascade continues:
   → Page 3 loads → items[36-53] accessed → Page 4 loads
   → Page 4 loads → items[54-71] accessed → Page 5 loads
   → Page 5 loads → items[72-89] accessed → Page 6 loads
   → Page 6 loads → items[90-107] accessed → Page 7 loads
```

**The problem:** Every time `items[index]` is called (even just to get a key), Paging3's `LazyPagingItems` tracks it as a "peek" operation, which signals demand for that data.

### ✅ New Approach: `key = items.itemKey { it.id }`

**What happens during initial composition:**

```
1. LazyColumn measures layout
   → "I need to display items 0-10 on screen"

2. For each visible item, LazyColumn calls key function:
   → itemKey(0) → Internally: "Get key for item 0"
   → itemKey(1) → Internally: "Get key for item 1"
   ...
   → itemKey(10) → Internally: "Get key for item 10"

3. itemKey() does NOT call items[index] directly:
   → It uses Paging3's internal snapshot
   → No "access tracking" is triggered
   → Paging3: "✅ Just providing keys, not indicating demand"

4. LazyColumn remeasures during layout passes:
   → itemKey() called again
   → Still no access tracking
   → Paging3: "✅ Still just keys, no prefetch needed"

5. Paging3 only prefetches when:
   → Items are actually COMPOSED (rendered to screen)
   → User scrolls within prefetchDistance of the end
   → In this case: Only when user scrolls to ~item 15-16
```

**The key difference:** `itemKey()` provides identity WITHOUT signaling demand. It's a "read-only view" that doesn't trigger Paging3's prefetch heuristics.

## Mechanical Explanation: Why It Happened & Why the Fix Works

### 🔧 Paging3's Internal Architecture

To understand why `items[index]` caused the issue, you need to know how `LazyPagingItems` tracks item access:

```kotlin
// Simplified internal structure of LazyPagingItems
class LazyPagingItems<T> {
    private val itemSnapshotList: ItemSnapshotList<T>

    // This is what gets called when you do items[index]
    operator fun get(index: Int): T? {
        // 1. Access tracking happens HERE
        itemSnapshotList.registerAccessHint(index)

        // 2. Return the item
        return itemSnapshotList[index]
    }

    // This is what itemKey() calls internally
    fun itemKey(keyFactory: (T) -> Any): (Int) -> Any {
        return { index ->
            // NO access tracking - just retrieves cached key
            itemSnapshotList.getKeyAt(index, keyFactory)
        }
    }
}
```

### 🎯 The Root Issue: `get()` vs `itemKey()`

#### What `items[index]` Actually Does:

```kotlin
key = { index -> items[index]?.id }
         ↓
    calls items.get(index)
         ↓
    triggers registerAccessHint(index)
         ↓
    Paging3 internal logic:
    "Item at index was accessed → user is viewing/approaching it"
         ↓
    Updates prefetch calculations
```

#### What `items.itemKey { it.id }` Does:

```kotlin
key = items.itemKey { it.id }
         ↓
    returns a lambda that calls getKeyAt(index)
         ↓
    NO registerAccessHint() call
         ↓
    Paging3 internal logic:
    "Just providing identity for already-loaded items"
         ↓
    No prefetch triggered
```

### 📊 Prefetch Distance Calculation

Paging3 uses this formula to decide when to load more pages:

```kotlin
// Simplified prefetch logic
fun shouldPrefetch(lastAccessedIndex: Int, itemCount: Int, prefetchDistance: Int): Boolean {
    val distanceFromEnd = itemCount - lastAccessedIndex
    return distanceFromEnd <= prefetchDistance
}
```

**Default `prefetchDistance`:** Typically 3-5 items from the end of loaded data.

#### With `items[index]` (Old Approach):

```
Loaded items: [0-17] (18 items from page 1)

LazyColumn calls key(0), key(1), ..., key(17) during measurement
  ↓
Each call triggers registerAccessHint()
  ↓
Paging3 sees: "lastAccessedIndex = 17"
  ↓
Calculation: itemCount(18) - lastAccessedIndex(17) = 1
  ↓
1 <= prefetchDistance(3) → TRUE → Load page 2

Page 2 arrives: [0-35] (36 items total)
  ↓
Layout recalculates → key() called for indices 18-35
  ↓
Paging3 sees: "lastAccessedIndex = 35"
  ↓
Calculation: itemCount(36) - lastAccessedIndex(35) = 1
  ↓
1 <= prefetchDistance(3) → TRUE → Load page 3

REPEAT 7 TIMES → 7 pages loaded immediately
```

#### With `items.itemKey { it.id }` (New Approach):

```
Loaded items: [0-17] (18 items from page 1)

LazyColumn calls itemKey(0), itemKey(1), ..., itemKey(17)
  ↓
NO registerAccessHint() calls
  ↓
Paging3 only tracks ACTUAL composition (when items render)
  ↓
Only items 0-10 actually compose (visible on screen)
  ↓
lastAccessedIndex = 10
  ↓
Calculation: itemCount(18) - lastAccessedIndex(10) = 8
  ↓
8 > prefetchDistance(3) → FALSE → No prefetch yet

User scrolls down to item 15
  ↓
Item 15 composes → registerAccessHint(15) called
  ↓
Calculation: itemCount(18) - lastAccessedIndex(15) = 3
  ↓
3 <= prefetchDistance(3) → TRUE → Load page 2 (correct behavior)
```

### 🧬 Why Multiple Layout Passes Amplified the Problem

Compose's layout system runs in multiple passes:

1. **Measurement pass:** Determines size of each item
2. **Placement pass:** Positions items on screen
3. **Recomposition pass:** Updates based on state changes

During each pass, LazyColumn calls the `key` lambda to maintain item identity. With `items[index]`:

```
Pass 1: key(0-17) called → 18 access hints → Prefetch triggered
  ↓
Pass 2: key(0-35) called → 36 access hints → More prefetch
  ↓
Pass 3: key(0-53) called → 54 access hints → Even more prefetch
```

This **multiplicative effect** caused the cascade of 7 pages loading instantly.

### ✅ Why `itemKey()` Solved It

The fix works because `itemKey()` is **explicitly designed** for this use case:

1. **Architectural Intent:** Paging3 authors knew that LazyColumn needs keys without triggering demand signals
2. **Optimized Code Path:** `itemKey()` bypasses the access tracking layer entirely
3. **Snapshot-Based:** Uses a cached snapshot of item keys, not live data access
4. **Decoupled Logic:** Separates "providing identity" from "measuring demand"

### 🔬 The Actual Code Path Difference

```
❌ Old: items[index]?.id
   └─> LazyPagingItems.get(index)
       └─> ItemSnapshotList.get(index)
           ├─> registerAccessHint(index)  ← PROBLEM
           └─> return item

✅ New: items.itemKey { it.id }
   └─> LazyPagingItems.itemKey(keyFactory)
       └─> Returns lambda that calls:
           └─> ItemSnapshotList.getKeyAt(index, keyFactory)
               └─> return cachedKey  ← No tracking
```

## Fix Applied
**Use Paging3's dedicated API:**

```kotlin
// ✅ CORRECT - Respects scroll-based prefetching
key = items.itemKey { it.id }
```

**Why this works:**
- `itemKey()` is Paging3's official API for providing stable keys to LazyColumn
- It doesn't trigger item access tracking, only provides identity when items are actually composed
- Paging3 only prefetches based on actual scroll position, not key generation

## Impact
- **Before:** 7 pages (~126 items) loaded on app start
- **After:** 1-2 pages (~18-36 items) loaded on app start, subsequent pages load on scroll
- **Performance improvement:** ~80% reduction in initial network requests

## Prevention
**Always use Paging3 extension APIs with `LazyPagingItems`:**
- ✅ `items.itemKey { it.id }`
- ✅ `items.itemCount`
- ❌ Manual index access: `items[index]`

## Reference
- Paging3 Compose docs: [Displaying paged data](https://developer.android.com/topic/libraries/architecture/paging/v3-compose)
- Related file: `app/src/main/java/com/example/cricfeedmobile/presentation/home/components/HomeFeedList.kt`