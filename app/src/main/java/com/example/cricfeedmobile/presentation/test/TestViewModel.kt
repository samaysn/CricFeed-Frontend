package com.example.cricfeedmobile.presentation.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cricfeedmobile.data.remote.CricbuzzApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ApiTestState {
    object Idle : ApiTestState()
    object Loading : ApiTestState()
    data class Success(val message: String) : ApiTestState()
    data class Error(val error: String) : ApiTestState()
}

@HiltViewModel
class TestViewModel @Inject constructor(
    private val apiService: CricbuzzApiService
) : ViewModel() {

    private val _state = MutableStateFlow<ApiTestState>(ApiTestState.Idle)
    val state: StateFlow<ApiTestState> = _state.asStateFlow()

    fun testHomeFeed() {
        viewModelScope.launch {
            _state.value = ApiTestState.Loading
            try {
                val response = apiService.getHomeFeed(page = 1, limit = 5)
                val message = buildString {
                    appendLine("✅ HOME FEED SUCCESS!")
                    appendLine()
                    appendLine("Total Items: ${response.pagination.totalItems}")
                    appendLine("Current Page: ${response.pagination.currentPage}")
                    appendLine("Total Pages: ${response.pagination.totalPages}")
                    appendLine()
                    appendLine("Feed Items (${response.data.size}):")
                    response.data.forEachIndexed { index, item ->
                        appendLine("${index + 1}. Type: ${item.type}, ID: ${item.id}")
                    }
                }
                _state.value = ApiTestState.Success(message)
            } catch (e: Exception) {
                _state.value = ApiTestState.Error(
                    "❌ Error: ${e.message}\n\n" +
                    "Stack trace:\n${e.stackTraceToString().take(500)}"
                )
            }
        }
    }

    fun testLiveMatches() {
        viewModelScope.launch {
            _state.value = ApiTestState.Loading
            try {
                val response = apiService.getLiveMatches()
                val message = buildString {
                    appendLine("✅ LIVE MATCHES SUCCESS!")
                    appendLine()
                    appendLine("Total Live Matches: ${response.liveMatches}")
                    appendLine()
                    response.liveMatches.forEachIndexed { index, match ->
                        appendLine("${index + 1}. ${match.title}")
                        appendLine("   Venue: ${match.venue}")
                        appendLine("   Status: ${match.status}")
                        appendLine()
                    }
                }
                _state.value = ApiTestState.Success(message)
            } catch (e: Exception) {
                _state.value = ApiTestState.Error(
                    "❌ Error: ${e.message}\n\n" +
                    "Stack trace:\n${e.stackTraceToString().take(500)}"
                )
            }
        }
    }

    fun testUpcomingMatches() {
        viewModelScope.launch {
            _state.value = ApiTestState.Loading
            try {
                val response = apiService.getUpcomingMatches(page = 1, limit = 5)
                val message = buildString {
                    appendLine("✅ UPCOMING MATCHES SUCCESS!")
                    appendLine()
                    appendLine("Total Matches: ${response.pagination.totalItems}")
                    appendLine("Current Page: ${response.pagination.currentPage}")
                    appendLine()
                    appendLine("Matches (${response.matches.size}):")
                    response.matches.forEachIndexed { index, match ->
                        appendLine("${index + 1}. ${match.team1?.name ?: "TBD"} vs ${match.team2?.name ?: "TBD"}")
                        appendLine("   Type: ${match.matchType}")
                        appendLine("   Venue: ${match.venue}")
                        appendLine()
                    }
                }
                _state.value = ApiTestState.Success(message)
            } catch (e: Exception) {
                _state.value = ApiTestState.Error(
                    "❌ Error: ${e.message}\n\n" +
                    "Stack trace:\n${e.stackTraceToString().take(500)}"
                )
            }
        }
    }

    fun testMatchResults() {
        viewModelScope.launch {
            _state.value = ApiTestState.Loading
            try {
                val response = apiService.getMatchResults(page = 1, limit = 5)
                val message = buildString {
                    appendLine("✅ MATCH RESULTS SUCCESS!")
                    appendLine()
                    appendLine("Total Results: ${response.pagination.totalItems}")
                    appendLine("Current Page: ${response.pagination.currentPage}")
                    appendLine()
                    appendLine("Results (${response.results.size}):")
                    response.results.forEachIndexed { index, result ->
                        appendLine("${index + 1}. ${result.title}")
                        appendLine("   Result: ${result.result}")
                        appendLine("   Type: ${result.matchType}")
                        appendLine()
                    }
                }
                _state.value = ApiTestState.Success(message)
            } catch (e: Exception) {
                _state.value = ApiTestState.Error(
                    "❌ Error: ${e.message}\n\n" +
                    "Stack trace:\n${e.stackTraceToString().take(500)}"
                )
            }
        }
    }

    fun testNews() {
        viewModelScope.launch {
            _state.value = ApiTestState.Loading
            try {
                val response = apiService.getNews(page = 1, limit = 5)
                val message = buildString {
                    appendLine("✅ NEWS SUCCESS!")
                    appendLine()
                    appendLine("Total Articles: ${response.pagination.totalItems}")
                    appendLine("Current Page: ${response.pagination.currentPage}")
                    appendLine()
                    appendLine("Articles (${response.articles.size}):")
                    response.articles.forEachIndexed { index, article ->
                        appendLine("${index + 1}. ${article.headline}")
                        appendLine("   Author: ${article.author.name}")
                        appendLine("   Category: ${article.category}")
                        appendLine()
                    }
                }
                _state.value = ApiTestState.Success(message)
            } catch (e: Exception) {
                _state.value = ApiTestState.Error(
                    "❌ Error: ${e.message}\n\n" +
                    "Stack trace:\n${e.stackTraceToString().take(500)}"
                )
            }
        }
    }

    fun testVideos() {
        viewModelScope.launch {
            _state.value = ApiTestState.Loading
            try {
                val response = apiService.getVideos(page = 1, limit = 5)
                val message = buildString {
                    appendLine("✅ VIDEOS SUCCESS!")
                    appendLine()
                    appendLine("Total Videos: ${response.pagination.totalItems}")
                    appendLine("Current Page: ${response.pagination.currentPage}")
                    appendLine()
                    appendLine("Videos (${response.videos.size}):")
                    response.videos.forEachIndexed { index, video ->
                        appendLine("${index + 1}. ${video.title}")
                        appendLine("   Duration: ${video.duration}")
                        appendLine("   Views: ${video.views}")
                        appendLine()
                    }
                }
                _state.value = ApiTestState.Success(message)
            } catch (e: Exception) {
                _state.value = ApiTestState.Error(
                    "❌ Error: ${e.message}\n\n" +
                    "Stack trace:\n${e.stackTraceToString().take(500)}"
                )
            }
        }
    }
}