package com.example.cricfeedmobile.presentation.matchResults

import androidx.lifecycle.ViewModel
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.cricfeedmobile.domain.repository.FeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.cricfeedmobile.domain.model.FeedItem
import com.example.cricfeedmobile.domain.model.MatchResult
import kotlinx.coroutines.flow.Flow

@HiltViewModel
class MatchResultsViewModel @Inject constructor(
    repository: FeedRepository
): ViewModel(){

    val matchResultFlow: Flow<PagingData<MatchResult>> = repository.getMatchResults().cachedIn(viewModelScope)

}