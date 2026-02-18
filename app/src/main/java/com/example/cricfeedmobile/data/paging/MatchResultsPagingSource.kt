package com.example.cricfeedmobile.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.cricfeedmobile.data.mapper.toDomain
import com.example.cricfeedmobile.data.remote.CricbuzzApiService
import com.example.cricfeedmobile.domain.model.FeedItem
import javax.inject.Inject
import com.example.cricfeedmobile.domain.model.MatchResult
import kotlinx.coroutines.delay

class MatchResultsPagingSource @Inject constructor(
    private val apiService: CricbuzzApiService
) : PagingSource<Int, MatchResult>() {


    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MatchResult> {
       return try {
          val currentPage = params.key ?: 1
         val response = apiService.getMatchResults(
             page = currentPage,
                limit = params.loadSize
         )

            val matchResults = response.results.map {
                it.toDomain()
            }

            val nextKey = if( response.pagination.hasNext){
                response.pagination.currentPage + 1
            }else {
                null
            }

            val prevKey = if (response.pagination.hasPrevious){
                response.pagination.currentPage - 1
            }else {
                null
            }

           delay(700)

           LoadResult.Page(
                data = matchResults,
                prevKey = prevKey,
                nextKey = nextKey
           )
        } catch (e : Exception) {
            LoadResult.Error(e)
        }
    }



    override fun getRefreshKey(state: PagingState<Int, MatchResult>): Int? {
        return state.anchorPosition?.let {
            anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }


}