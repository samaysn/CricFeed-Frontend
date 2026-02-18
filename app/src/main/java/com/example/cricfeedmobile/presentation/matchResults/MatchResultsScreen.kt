package com.example.cricfeedmobile.presentation.matchResults

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.cricfeedmobile.presentation.home.components.MatchResultCard
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchResultsScreen(
    viewModel : MatchResultsViewModel = hiltViewModel()
)
{
    DisposableEffect(Unit) {
        Log.d("MatchResultsScreen", "ENTER composition")

        onDispose {
            Log.d("MatchResultsScreen", "LEAVE composition")
        }
    }

    LaunchedEffect(Unit) {
        Log.d("MatchResultsScreen", "LaunchedEffect executed")
    }

    val matchResults = viewModel.matchResultFlow.collectAsLazyPagingItems()

    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Results") },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                state = listState,

                ) {
                items(
                    count = matchResults.itemCount,
                    key = matchResults.itemKey { "upcoming_match-${it.matchId}-${it.matchType}" },
                    contentType = {
                        "upcoming_match"
                    }
                ) { index ->
                    val match = matchResults[index]

                    Card(
                        modifier = Modifier.fillMaxWidth()
//                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .height(250.dp)
                            .padding(horizontal = 20.dp, vertical = 10.dp),
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
                       ){
                           Column (
//                               modifier = Modifier.fillMaxSize(),
//                            contentAlignment = Alignment.Center
                           ) {
                               Text(
                                   text = match?.title ?: "NO TITLE"
                               )

                               match?.result?.let {
                                   Text(
                                       text = it
                                   )
                               }
                           }
                       }
                    }
                }

                item {
                    if (matchResults.loadState.append is LoadState.Loading) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                item {
                    if (matchResults.loadState.refresh is LoadState.Loading) {
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
    }}