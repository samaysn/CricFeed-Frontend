package com.example.cricfeedmobile.data.remote

import com.example.cricfeedmobile.data.remote.dto.FeedResponseDto
import com.example.cricfeedmobile.data.remote.dto.LiveMatchesResponseDto
import com.example.cricfeedmobile.data.remote.dto.MatchResultsResponseDto
import com.example.cricfeedmobile.data.remote.dto.NewsResponseDto
import com.example.cricfeedmobile.data.remote.dto.UpcomingMatchesResponseDto
import com.example.cricfeedmobile.data.remote.dto.VideosResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface CricbuzzApiService {

    @GET("feed/home")
    suspend fun getHomeFeed(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): FeedResponseDto

    @GET("matches/upcoming")
    suspend fun getUpcomingMatches(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): UpcomingMatchesResponseDto

    @GET("matches/live")
    suspend fun getLiveMatches(): LiveMatchesResponseDto

    @GET("matches/results")
    suspend fun getMatchResults(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ) : MatchResultsResponseDto

    @GET("news")
    suspend fun getNews(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("category") category: String? = null
    ): NewsResponseDto


    @GET("videos")
    suspend fun getVideos(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("type") type: String? = null
    ): VideosResponseDto

}