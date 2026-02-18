package com.example.cricfeedmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.cricfeedmobile.presentation.home.HomeScreen
import com.example.cricfeedmobile.ui.theme.CricFeedMobileTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.cricfeedmobile.presentation.matchResults.MatchResultsScreen
import com.example.cricfeedmobile.presentation.navigation.AppNavGraph
import com.example.cricfeedmobile.presentation.navigation.CricFeedBottomNavBar
import com.example.cricfeedmobile.presentation.navigation.Routes
import com.example.cricfeedmobile.presentation.upcoming.UpcomingMatchesScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CricFeedMobileTheme {
//                TestScreen()
//                NavigationStack()
                AppNavGraph()
            }
        }
    }
}

//@Composable
//fun NavigationStack(){
//    val navController = rememberNavController()
//
//
//    Scaffold(
//        bottomBar = {
//            val navBackStackEntry by navController.currentBackStackEntryAsState()
//            val currentRoute = navBackStackEntry?.destination?.route
//            val bottomBarRoutes = setOf(
//                Routes.HOME,
//                Routes.MATCH_RESULTS,
//                Routes.UPCOMING_MATCHES
//
//            )
//
//            if (currentRoute in bottomBarRoutes){
//                CricFeedBottomNavBar(navController = navController)
//            }
//        }
//    ) {innerPadding ->
//        NavHost(
//            navController = navController,
//            startDestination = Routes.HOME,
//            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
//        ){
//            composable(Routes.HOME) {
//                HomeScreen(
//                    viewModel = hiltViewModel(),
//                    navController = navController
//                )
//            }
//
//            composable(Routes.MATCH_RESULTS) {
//                MatchResultsScreen(
//                    viewModel = hiltViewModel()
//                )
//            }
//
//            composable(Routes.UPCOMING_MATCHES) {
//                UpcomingMatchesScreen(
//                    homeViewModel = hiltViewModel(),
//                    onBackClick = { navController.navigateUp() }
//                )
//            }
//        }
//    }
//}