package com.example.cricfeedmobile.presentation.home.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LOG_TAG
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.example.cricfeedmobile.domain.model.UpcomingMatch

@Composable
fun UpcomingMatchCard(match: UpcomingMatch) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(160.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFFE3F2FD)
            ) {
                Text(
                    text = "UPCOMING",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF1976D2),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Match title
            Text(
                text = match.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Match timing
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏰",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = match.startTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Venue
            Text(
                text = match.venue,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9E9E9E),
                maxLines = 1
            )
        }
    }
}

@Composable
fun UpcomingMatchesCarouselComponent(
    upcomingMatches: LazyPagingItems<UpcomingMatch>,
    onClick : () -> Unit
) {
    val listState = rememberLazyListState()

    Column (
        modifier = Modifier.fillMaxWidth()
    ) {
        // Section header
        Text(
            text = "Upcoming Matches",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        LazyRow (
//            modifier = Modifier.height(160.dp),
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            flingBehavior = rememberSnapFlingBehavior(listState),
        ) {

            items(
                count = upcomingMatches.itemCount,
                key = upcomingMatches.itemKey { "carousel_${it.matchId}" },
                contentType = {
                    "upcoming_match"
                }
            ) { index ->
                val match = upcomingMatches[index] ?: return@items
                UpcomingMatchCard(match)
            }

            if (upcomingMatches.loadState.append is LoadState.Loading) {
                item {
                    Box( modifier = Modifier
                        .width(80.dp)
                        .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = Color(0xFF1976D2)
                        )
                    }
                }
            }

            if (upcomingMatches.loadState.append.endOfPaginationReached) {
                item {
                   Card(
                       modifier = Modifier
                           .height(160.dp),
                       shape = RoundedCornerShape(12.dp),
                       colors = CardDefaults.cardColors(
                           containerColor = Color.White
                       ),
                       elevation = CardDefaults.cardElevation(
                           defaultElevation = 2.dp,
                       )
                   ) { Box( modifier = Modifier.clickable(true, onClick =
                       {
                           onClick()
                       })
                       .width(80.dp)
                       .height(160.dp),
                       contentAlignment = Alignment.Center,

                       ) {

                       Text(
                           text = "View All"

                       )

                   }}
                }
            }
        }
    }
}
