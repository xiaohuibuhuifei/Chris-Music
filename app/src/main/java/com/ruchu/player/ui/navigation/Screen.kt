package com.ruchu.player.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Library : Screen("library")
    data object AlbumDetail : Screen("album/{albumId}") {
        fun createRoute(albumId: String) = "album/$albumId"
    }
    data object Player : Screen("player")
}
