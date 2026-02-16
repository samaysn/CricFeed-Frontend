package com.example.cricfeedmobile.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.example.cricfeedmobile.domain.model.FeedItem
import com.example.cricfeedmobile.domain.model.UpcomingMatch
import com.example.cricfeedmobile.presentation.home.VideoHighlightCard

@Composable
fun HomeFeedList(
    items: LazyPagingItems<FeedItem>,
    upcomingItems: LazyPagingItems<UpcomingMatch>
) {
    val listState = rememberLazyListState()


    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            count = items.itemCount,
            key = items.itemKey { it.id }
//            key = {index -> items[index]?.id ?: index }
        ) { index ->
            val item = items[index] ?: return@items

            when (item) {
                is FeedItem.UpcomingMatchesCarousel -> {
                        UpcomingMatchesCarouselComponent(upcomingItems)
                }

                is FeedItem.LiveMatch -> {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        LiveMatchCard(item)
                    }
                }

                is FeedItem.NewsArticle -> {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        NewsArticleCard(item)
                    }
                }

                is FeedItem.MatchResult -> {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        MatchResultCard(item)
                    }
                }

                is FeedItem.BannerAd -> {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        BannerAdCard(item)
                    }
                }

                is FeedItem.VideoHighlight -> {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        VideoHighlightCard(item)
                    }
                }
            }
        }

        // Pagination footer with styled loading indicator
        if (items.loadState.append is LoadState.Loading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF1976D2),
                        strokeWidth = 3.dp
                    )
                }
            }
        }
    }
}
