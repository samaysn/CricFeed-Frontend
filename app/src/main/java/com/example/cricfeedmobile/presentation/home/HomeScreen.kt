package com.example.cricfeedmobile.presentation.home

import android.R
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.cricfeedmobile.domain.usecase.GetUpcomingMatchesUseCase
import com.example.cricfeedmobile.presentation.home.components.HomeFeedList
import com.example.cricfeedmobile.presentation.navigation.Routes
import okhttp3.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    navController : NavController
) {
    val feedItems = viewModel.homeFeedFlow.collectAsLazyPagingItems()
    val upcomingItems = viewModel.upcomingMatchesFlow.collectAsLazyPagingItems()

    // DEBUG LOGS
    Log.d("HomeScreen", "ItemCount: ${feedItems.itemCount}")
    Log.d("HomeScreen", "Refresh: ${feedItems.loadState.refresh}")
    Log.d("HomeScreen", "Append: ${feedItems.loadState.append}")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("CricFeed") })
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .background(Color.Gray)
        ) {

            HomeFeedList(
                items = feedItems,
                upcomingItems = upcomingItems,
                onClick = {
                    navController.navigate(Routes.UPCOMING_MATCHES)
                }
            )

            if (feedItems.loadState.refresh is LoadState.Loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            if (feedItems.loadState.refresh is LoadState.Error) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error loading feed")
                }
            }
        }
    }
}
