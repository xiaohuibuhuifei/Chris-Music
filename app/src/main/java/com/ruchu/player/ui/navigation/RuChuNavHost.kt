package com.ruchu.player.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ruchu.player.ui.screen.home.HomeScreen
import com.ruchu.player.ui.screen.library.LibraryScreen
import com.ruchu.player.ui.screen.album.AlbumDetailScreen
import com.ruchu.player.ui.screen.player.PlayerScreen

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun RuChuNavHost() {
    val navController = rememberNavController()
    var showPlayer by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = showPlayer) {
        showPlayer = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToLibrary = { navController.navigate(Screen.Library.route) },
                    onNavigateToPlayer = { showPlayer = true },
                    onNavigateToAlbum = { albumId ->
                        navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                    }
                )
            }

            composable(Screen.Library.route) {
                LibraryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPlayer = { showPlayer = true }
                )
            }

            composable(
                route = Screen.AlbumDetail.route,
                arguments = listOf(navArgument("albumId") { type = NavType.StringType })
            ) { backStackEntry ->
                val albumId = backStackEntry.arguments?.getString("albumId") ?: ""
                AlbumDetailScreen(
                    albumId = albumId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPlayer = { showPlayer = true }
                )
            }
        }

        // Player overlay - slides up from bottom
        AnimatedVisibility(
            visible = showPlayer,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            PlayerScreen(onNavigateBack = { showPlayer = false })
        }
    }
}
