package com.example.cricfeedmobile.domain.model

sealed class FeedItem {
    abstract val id: String
    abstract val timestamp: Long

    data class LiveMatch(
        override val id: String,
        override val timestamp: Long,
        val matchId: Int,
        val title: String,
        val venue: String,
        val status: String,
        val matchType: String,
        val seriesName: String,
        val team1: TeamScore,
        val team2: TeamScore,
        val liveText: String,
        val currentBatsmen: List<Batsman> = emptyList(),
        val currentBowler: Bowler? = null,
        val lastWicket: String? = null,
        val recentBalls: List<String> = emptyList(),
        val startedAt: String
    ) : FeedItem()

    data class UpcomingMatchesCarousel(
        override val id: String,
        override val timestamp: Long,
        val title: String,
        val matches: List<UpcomingMatch>,  // Preview matches (5 items)
        val totalCount: Int,                // Total available (e.g., 48)
        val paginationEndpoint: String      // "/api/matches/upcoming"
    ) : FeedItem()

    data class NewsArticle(
        override val id: String,
        override val timestamp: Long,
        val articleId: String,
        val headline: String,
        val summary: String,
        val thumbnailUrl: String? = null,
        val author: Author,
        val publishedAt: String,
        val category: String,
        val readTime: String? = null
    ) : FeedItem()

    data class VideoHighlight(
        override val id: String,
        override val timestamp: Long,
        val videoId: Int,
        val title: String,
        val thumbnailUrl: String,
        val duration: String,
        val views: Int,
        val uploadedAt: String,
        val videoUrl: String,
        val matchContext: MatchContext? = null
    ) : FeedItem()

    data class MatchResult(
        override val id: String,
        override val timestamp: Long,
        val matchId: String,
        val title: String,
        val matchType: String,
        val team1: Team,
        val team2: Team,
//        val team1Score: String,
//        val team2Score: String,
        val result: String,
        val playerOfMatch: Player? = null,
        val completedAt: String,
        val venue : String?
    ) : FeedItem()

    data class BannerAd(
        override val id: String,
        override val timestamp: Long,
        val imageUrl: String,
        val targetUrl: String,
        val priority: Int?
    ) : FeedItem()
}