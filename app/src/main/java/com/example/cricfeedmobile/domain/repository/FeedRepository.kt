package com.example.cricfeedmobile.domain.repository

import androidx.paging.PagingData
import com.example.cricfeedmobile.domain.model.FeedItem
import com.example.cricfeedmobile.domain.model.UpcomingMatch
import kotlinx.coroutines.flow.Flow
import com.example.cricfeedmobile.domain.model.MatchResult

interface FeedRepository {
    fun getHomeFeed(): Flow<PagingData<FeedItem>>

    fun getUpcomingMatches(): Flow<PagingData<UpcomingMatch>>

    fun getMatchResults(): Flow<PagingData<MatchResult>>

    // fun getNews(category: String? = null): Flow<PagingData<NewsArticle>>

    // fun getVideos(type: String? = null): Flow<PagingData<VideoHighlight>>
}