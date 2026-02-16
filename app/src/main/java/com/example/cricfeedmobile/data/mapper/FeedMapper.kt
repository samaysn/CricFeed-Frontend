package com.example.cricfeedmobile.data.mapper

import android.util.Log
import com.example.cricfeedmobile.data.remote.dto.*
import com.example.cricfeedmobile.domain.model.*
import kotlinx.serialization.json.Json

fun FeedItemDto.toDomain(): FeedItem? {
    android.util.Log.d("FeedMapper", "Mapping feed item - type: '$type', id: '$id', timestamp: '$timestamp'")

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    val result = try {
        when (type) {
            "live_match" -> {
                val liveMatchDto = json.decodeFromJsonElement(LiveMatchDto.serializer(), data)
                liveMatchDto.toDomain(id, timestamp)
            }
            "upcoming_matches_carousel" -> {
                val carouselDto = json.decodeFromJsonElement(UpcomingMatchesCarouselDto.serializer(), data)
                carouselDto.toDomain(id, timestamp)
            }
            "news_article" -> {
                val newsDto = json.decodeFromJsonElement(NewsDto.serializer(), data)
                newsDto.toDomain(id, timestamp)
            }
            "video_highlight" -> {
                val videoDto = json.decodeFromJsonElement(VideoDto.serializer(), data)
                videoDto.toDomain(id, timestamp)
            }
            "match_result" -> {
                val matchResultDto = json.decodeFromJsonElement(MatchResultDto.serializer(), data)
                matchResultDto.toDomain(id, timestamp)
            }
            "banner_ad" -> {
                val bannerAdDto = json.decodeFromJsonElement(BannerAdDto.serializer(), data)
                bannerAdDto.toDomain(id, timestamp)
            }
            else -> {
                null
            }
        }
    } catch (e: Exception) {
        null
    }

    if (result == null) {
        Log.w("FeedMapper", "❌ Mapping returned null for type '$type'")
    } else {
        Log.d("FeedMapper", "✅ Successfully mapped '$type' to ${result::class.simpleName}")
    }

    return result
}

fun LiveMatchDto.toDomain(id: String, timestamp: Long): FeedItem.LiveMatch {
    return FeedItem.LiveMatch(
        id = id,
        timestamp = timestamp,
        matchId = matchId,
        title = title,
        venue = venue,
        status = status,
        matchType = matchType,
        seriesName = seriesName,
        team1 = team1.toDomain(),
        team2 = team2.toDomain(),
        liveText = liveText,
        currentBatsmen = currentBatsmen?.map { it.toDomain() } ?: emptyList(),
        currentBowler = currentBowler?.toDomain(),
        lastWicket = lastWicket,
        recentBalls = recentBalls,
        startedAt = startedAt
    )
}

fun TeamScoreDto.toDomain(): TeamScore {
    return TeamScore(
        name = name,
        shortName = shortName,
        logo = logo,
        score = score,
        overs = overs,
        runRate = runRate
    )
}

fun BatsmanDto.toDomain(): Batsman {
    return Batsman(
        name = name,
        runs = runs,
        balls = balls,
        fours = fours,
        sixes = sixes
    )
}

fun BowlerDto.toDomain(): Bowler {
    return Bowler(
        name = name,
        overs = overs,
        maidens = maidens,
        runs = runs,
        wickets = wickets
    )
}


fun UpcomingMatchesCarouselDto.toDomain(id: String, timestamp: Long): FeedItem.UpcomingMatchesCarousel {
    return FeedItem.UpcomingMatchesCarousel(
        id = id,
        timestamp = timestamp,
        title = "Upcoming Matches",
        matches = matches.map { it.toDomain() },
        totalCount = totalCount,
        paginationEndpoint = "/api/matches/upcoming"
    )
}

fun UpcomingMatchDto.toDomain(): UpcomingMatch {
    return UpcomingMatch(
        matchId = matchId,
        title = title,
        venue = venue,
        startTime = startTime,
        team1 = team1.toDomain(),
        team2 = team2.toDomain(),
        matchType = matchType,
        seriesName = seriesName,
        isNotificationSet = isNotificationSet
    )
}

fun TeamDto.toDomain(): Team {
    return Team(
        name = name,
        shortName = shortName,
        logo = logo
    )
}


fun NewsDto.toDomain(id: String, timestamp: Long): FeedItem.NewsArticle {
    return FeedItem.NewsArticle(
        id = id,
        timestamp = timestamp,
        articleId = articleId,
        headline = headline,
        summary = summary,
        thumbnailUrl = thumbnailUrl,
        author = author.toDomain(),
        publishedAt = publishedAt,
        category = category,
        readTime = readTime
    )
}

fun AuthorDto.toDomain(): Author {
    return Author(
        name = name,
        avatarUrl = avatarUrl
    )
}

fun VideoDto.toDomain(id: String, timestamp: Long): FeedItem.VideoHighlight {
    return FeedItem.VideoHighlight(
        id = id,
        timestamp = timestamp,
        videoId = videoId,
        title = title,
        thumbnailUrl = thumbnailUrl,
        duration = duration,
        views = views,
        uploadedAt = uploadedAt,
        videoUrl = videoUrl,
        matchContext = matchContext?.toDomain()
    )
}

fun MatchContextDto.toDomain(): MatchContext {
    return MatchContext(
        matchId = matchId,
        matchTitle = title ?: ""
    )
}


fun MatchResultDto.toDomain(id: String, timestamp: Long): FeedItem.MatchResult {
    return FeedItem.MatchResult(
        id = id,
        timestamp = timestamp,
        matchId = matchId,
        title = title,
        matchType = matchType,
        team1 = team1.toDomain(),
        team2 = team2.toDomain(),
//        team1Score = team1.score,
//        team2Score = team2.score,
        result = result,
        playerOfMatch = playerOfMatch?.toDomain(),
        completedAt = completedAt,
        venue = venue
    )
}

fun PlayerDto.toDomain(): Player {
    return Player(
        playerId = playerId ?: "",
        name = name,
        avatarUrl = avatarUrl
    )
}

fun BannerAdDto.toDomain(id: String, timestamp: Long): FeedItem.BannerAd {
    return FeedItem.BannerAd(
        id = id,
        timestamp = timestamp,
        imageUrl = imageUrl,
        targetUrl = targetUrl,
        priority = priority
    )
}

fun MatchResultDto.toDomain(): MatchResult {
    return MatchResult(
        matchId = matchId,
        title = title,
        matchType = matchType,
        team1 = team1.toDomain(),
        team2 = team2.toDomain(),
        result = result,
        playerOfMatch = playerOfMatch?.toDomain(),
        completedAt = completedAt
    )
}

