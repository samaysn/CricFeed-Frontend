package com.example.cricfeedmobile.data.paging

import android.util.Log
import androidx.paging.LOG_TAG
import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import com.example.cricfeedmobile.data.mapper.toDomain
import com.example.cricfeedmobile.data.remote.CricbuzzApiService
import com.example.cricfeedmobile.domain.model.UpcomingMatch
import kotlinx.coroutines.delay
import java.lang.Exception


class UpcomingMatchesWithPreviewPagingSource (
    private val apiService: CricbuzzApiService,
    private val previewMatches: List<UpcomingMatch>,
) : PagingSource<Int, UpcomingMatch>(){

    init {
        Log.d("NEW-PAGING-1", previewMatches.size.toString())
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UpcomingMatch> {
        return try {
            val page = params.key ?: 1

            if( page == 1){
                val response = apiService.getUpcomingMatches(page = 1, limit = params.loadSize)
                val apiMatches = response.matches.map { it.toDomain() }


                val mergedMatches = (previewMatches + apiMatches)
                    .distinctBy {
                        it.matchId
                    }

                Log.d("NEW-PAGING", "PREVIEW PAGE SUCCESS MERGED : ${
                    mergedMatches.size
                }, apiMatches : ${
                    apiMatches.size
                }, apiService : ${
                    response.matches.size
                }, previewMatches : ${previewMatches.size}")

                LoadResult.Page(
                    data = mergedMatches,
                    prevKey = null,
                    nextKey = if(response.pagination.hasNext) 2 else null
                )
            } else {
                val response = apiService.getUpcomingMatches(page, limit = params.loadSize )
                val matches = response.matches.map { it.toDomain() }

                Log.d("NEW-PAGING", "NEW PAGE FETCH SUCCESS ${
                    matches.size
                }")

                delay(1000)
                LoadResult.Page(
                    data = matches,
                    prevKey = if(page > 1) page - 1 else null,
                    nextKey = if(response.pagination.hasNext) page + 1 else null
                )
            }
        } catch (e : Exception){
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, UpcomingMatch>): Int? {
        return state.anchorPosition?.let {
            position ->
            state.closestPageToPosition(position)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(position)?.nextKey?.minus(1)
        }
    }
}