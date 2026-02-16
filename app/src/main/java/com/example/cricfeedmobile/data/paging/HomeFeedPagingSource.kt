package com.example.cricfeedmobile.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.cricfeedmobile.data.mapper.toDomain
import com.example.cricfeedmobile.data.remote.CricbuzzApiService
import com.example.cricfeedmobile.domain.model.FeedItem
import kotlinx.coroutines.delay

class HomeFeedPagingSource(
    private val api: CricbuzzApiService
) : PagingSource<Int, FeedItem>() {
    init {
        Log.d("PS_DEBUG", "HomeFeedPagingSource CREATED: ${this.hashCode()}")
    }
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FeedItem> {
        return try {
            val page = params.key ?: 1
            val loadSize = params.loadSize

            val response = api.getHomeFeed(
                page = page,
                limit = loadSize
            )

            val feedItems = response.data
                .mapNotNull { it.toDomain() }

            val isEndReached =
                feedItems.isEmpty() || feedItems.size < loadSize || !response.pagination.hasNext

            Log.d(
                "pagination-debug",
                "page=$page loadSize=$loadSize items=${feedItems.size} endReached=$isEndReached"
            )

            delay(2000)
            LoadResult.Page(
                data = feedItems,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (isEndReached) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, FeedItem>): Int? {
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.let { page ->
                page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
            }
        }
    }
}
