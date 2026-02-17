package com.example.cricfeedmobile.presentation.upcoming

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems
import com.example.cricfeedmobile.domain.model.UpcomingMatch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingMatchesScreen (
    upcomingMatches : LazyPagingItems<UpcomingMatch>,
    onBackClick : () -> Unit
){
    Text(
        text = "UpcomingMatchesScreen"
    )
}