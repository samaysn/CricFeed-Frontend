package com.example.cricfeedmobile.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    upcomingItems: LazyPagingItems<UpcomingMatch>,
    onClick : () -> Unit
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
//            key = items.itemKey { it.id },
            key = items.itemKey { item ->
                when (item) {
                    is FeedItem.LiveMatch ->
                        "live_${item.id}"
                    is FeedItem.UpcomingMatchesCarousel ->
                        "carousel_${item.id}"
                    is FeedItem.NewsArticle ->
                        "news_${item.id}"
                    is FeedItem.MatchResult ->
                        "result_${item.id}"
                    is FeedItem.BannerAd ->
                        "ad_${item.id}"
                    is FeedItem.VideoHighlight ->
                        "video_${item.id}"
                }
            },
//            key = {index -> items[index]?.id ?: index }, // BUG : Aggressive prefetching at initial load
            contentType = { index ->
                when (items[index]) {
                    is FeedItem.LiveMatch ->
                        "live_match"
                    is FeedItem.UpcomingMatchesCarousel
                        -> "carousel"
                    is FeedItem.NewsArticle ->
                        "news_article"
                    is FeedItem.MatchResult ->
                        "match_result"
                    is FeedItem.BannerAd -> "banner_ad"
                    is FeedItem.VideoHighlight ->
                        "video_highlight"
                    null -> "unknown"
                }
            }

        ) { index ->
            val item = items[index] ?: return@items

            when (item) {
                is FeedItem.UpcomingMatchesCarousel -> {
                        UpcomingMatchesCarouselComponent(upcomingItems, onClick)
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


        if(items.loadState.append.endOfPaginationReached){
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ){
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "END REACHED",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
        }

    }
}
