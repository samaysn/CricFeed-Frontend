package com.example.cricfeedmobile.domain.model

data class TeamScore(
    val name: String,
    val shortName: String,
    val logo: String,
    val score: String,
    val overs: String,
    val runRate: String? = null
)
data class Batsman(
    val name: String,
    val runs: Int,
    val balls: Int,
    val fours: Int,
    val sixes: Int
)

data class Bowler(
    val name: String,
    val overs: String,
    val maidens: Int,
    val runs: Int,
    val wickets: Int
)

data class UpcomingMatch(
    val matchId: Int,
    val title: String,
    val venue: String,
    val startTime: String,
    val team1: Team,
    val team2: Team,
    val matchType: String,
    val seriesName: String,
    val isNotificationSet: Boolean
)

data class Team(
    val name: String,
    val shortName: String,
    val logo: String
)

data class Author(
    val name: String,
    val avatarUrl: String? = null
)
data class Player(
    val playerId: String? = null,
    val name: String,
    val avatarUrl: String? = null
)

data class MatchContext(
    val matchId: String,
    val matchTitle: String? = ""
)

data class MatchResult(
    val matchId: String,
    val title: String,
    val matchType: String,
    val team1: Team,
    val team2: Team,
    val result: String,
    val playerOfMatch: Player?,
    val completedAt: String
)