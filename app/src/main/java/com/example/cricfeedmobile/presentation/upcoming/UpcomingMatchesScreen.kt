package com.example.cricfeedmobile.presentation.upcoming

import android.R
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.cricfeedmobile.domain.model.UpcomingMatch
import com.example.cricfeedmobile.presentation.home.HomeViewModel
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingMatchesScreen (
    homeViewModel: HomeViewModel = hiltViewModel(),
    onBackClick : () -> Unit
){
    val upcomingMatches = homeViewModel.upcomingMatchesFlow.collectAsLazyPagingItems()

    val listState = LazyListState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API Test Screen") },

                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },

    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(
                paddingValues
            ).fillMaxSize()

        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                state = listState,
//                flingBehavior = rememberSnapFlingBehavior(listState)

            ) {
                items(
                    count = upcomingMatches.itemCount,
                    key = upcomingMatches.itemKey { "upcoming_match-${it.matchId}-${it.matchType}"},
                    contentType = {
                        "upcoming_match"
                    }
                ){ index ->
                   val match = upcomingMatches[index]

                    Card(
                        modifier = Modifier.fillMaxWidth()
//                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .height(250.dp)
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                        ,
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        ),


                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = match?.title ?: "NO TITLE"
                            )
                        }
                    }
                }

                item {
                    if (upcomingMatches.loadState.append is LoadState.Loading) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                item {
                    if (upcomingMatches.loadState.refresh is LoadState.Loading){
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                }

            }
        }

    }

}