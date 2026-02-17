package com.example.cricfeedmobile.domain.usecase

import androidx.compose.runtime.Composable
import androidx.paging.PagingData
import androidx.paging.PagingLogger
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.cricfeedmobile.domain.model.UpcomingMatch
import com.example.cricfeedmobile.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUpcomingMatchesUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke() : Flow<PagingData<UpcomingMatch>>{
        return feedRepository.getUpcomingMatches()
    }
}