package com.example.cricfeedmobile.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.cricfeedmobile.data.mapper.toDomain
import com.example.cricfeedmobile.data.remote.CricbuzzApiService
import com.example.cricfeedmobile.domain.model.UpcomingMatch
import javax.inject.Inject

class UpcomingMatchesPagingSource @Inject constructor(
    private val apiService: CricbuzzApiService
) : PagingSource<Int, UpcomingMatch>()  {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UpcomingMatch> {
        Log.d("UpcomingPaging", "load page=${params.key}")
        return try {
            val currentPage = params.key ?: 1

            val response = apiService.getUpcomingMatches(
                page = currentPage,
                limit = params.loadSize
            )

            val upcomingMatches = response.matches.map {
                it.toDomain()
            }

            val nextKey = if (
                response.pagination.hasNext
            ){
                response.pagination.currentPage + 1
            }else{
                null
            }

            val prevKey = if (
                response.pagination.hasPrevious
            ){
                response.pagination.currentPage - 1
            }else {
                null
            }

            LoadResult.Page(
                data = upcomingMatches,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e : Exception) {
            LoadResult.Error(e)
        }
    }
    

    override fun getRefreshKey(
        state: PagingState<Int, UpcomingMatch>
    ): Int? {
        return state.anchorPosition?.let {
            anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

}