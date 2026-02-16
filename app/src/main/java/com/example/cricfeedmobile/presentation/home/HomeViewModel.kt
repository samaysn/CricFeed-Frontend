package com.example.cricfeedmobile.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.cricfeedmobile.domain.model.FeedItem
import com.example.cricfeedmobile.domain.model.MatchResult
import com.example.cricfeedmobile.domain.model.UpcomingMatch
import com.example.cricfeedmobile.domain.repository.FeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val feedRepository: FeedRepository
) : ViewModel(){

    val homeFeedFlow: Flow<PagingData<FeedItem>> = feedRepository
        .getHomeFeed()
        .cachedIn(viewModelScope)

//    val upcomingMatchesFlow : Flow<PagingData<UpcomingMatch>> = feedRepository
//        .getUpcomingMatches()
//        .cachedIn(viewModelScope)
val upcomingMatchesFlow : Flow<PagingData<UpcomingMatch>> = feedRepository
            .getUpcomingMatches()
        .cachedIn(viewModelScope)

    
    val matchResultFlow : Flow<PagingData<MatchResult>> = feedRepository
        .getMatchResults()
        .cachedIn(viewModelScope)
}