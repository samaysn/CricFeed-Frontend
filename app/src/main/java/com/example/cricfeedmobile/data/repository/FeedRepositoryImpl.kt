package com.example.cricfeedmobile.data.repository

import android.util.Log
import androidx.compose.foundation.pager.PageSize
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.cricfeedmobile.data.paging.HomeFeedPagingSource
import com.example.cricfeedmobile.data.paging.MatchResultsPagingSource
import com.example.cricfeedmobile.data.paging.UpcomingMatchesPagingSource
import com.example.cricfeedmobile.data.paging.UpcomingMatchesWithPreviewPagingSource
import com.example.cricfeedmobile.data.remote.CricbuzzApiService
import com.example.cricfeedmobile.domain.model.FeedItem
import com.example.cricfeedmobile.domain.model.UpcomingMatch
import com.example.cricfeedmobile.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import com.example.cricfeedmobile.domain.model.MatchResult
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val apiService: CricbuzzApiService
) : FeedRepository {

    private var upcomingMatchPreview: List<UpcomingMatch> = emptyList()


    override fun getHomeFeed(): Flow<PagingData<FeedItem>> {

        return Pager(
            config = PagingConfig(pageSize = 50,
                prefetchDistance = 1,
                initialLoadSize = 18,
                enablePlaceholders = false),
            pagingSourceFactory = { HomeFeedPagingSource(apiService)}
        ).flow.map { pagingData ->
            pagingData.map {  feedItem ->
                if(feedItem is FeedItem.UpcomingMatchesCarousel){
                    upcomingMatchPreview = feedItem.matches
                    Log.d("NEW-PAGING", upcomingMatchPreview.size.toString())
                }

                feedItem
            }
        }

    }


    override fun getUpcomingMatches(): Flow<PagingData<UpcomingMatch>> {

        return Pager(
            config = PagingConfig(
                pageSize = 4,
                initialLoadSize = 4,
                prefetchDistance = 1
            ),
            pagingSourceFactory = {
                UpcomingMatchesWithPreviewPagingSource(
                    apiService = apiService,
                    previewMatches = upcomingMatchPreview
                )
            }
        ).flow
    }






//    override fun getHomeFeed(): Flow<PagingData<FeedItem>> {
//        return Pager(
//            config = PagingConfig(
//                pageSize = 18,
//                prefetchDistance = 6,
//                initialLoadSize = 18,
//                enablePlaceholders = false
//            ),
//            pagingSourceFactory = {
//                HomeFeedPagingSource(apiService)
//            }
//        ).flow
//    }

//    override fun getUpcomingMatches(): Flow<PagingData<UpcomingMatch>> {
//        return Pager(
//            config = PagingConfig(
//                pageSize = 5,
//                prefetchDistance = 1,
//                enablePlaceholders = false,
//                initialLoadSize = 5
//            ),
//            pagingSourceFactory = {
//                UpcomingMatchesPagingSource(apiService)
//            }
//        ).flow
//    }

    override fun getMatchResults(): Flow<PagingData<MatchResult>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                prefetchDistance = 1,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                MatchResultsPagingSource(apiService)
            }
        ).flow
    }

}