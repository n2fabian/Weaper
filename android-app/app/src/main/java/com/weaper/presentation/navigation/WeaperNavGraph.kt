package com.weaper.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.weaper.presentation.home.HomeScreen
import com.weaper.presentation.player.PlaylistPlayerScreen
import com.weaper.presentation.playlist.PlaylistCreationScreen
import com.weaper.presentation.playlist.PlaylistListScreen
import com.weaper.presentation.settings.SettingsScreen
import com.weaper.presentation.soundboard.SoundboardScreen
import com.weaper.presentation.sync.SyncScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Playlists : Screen("playlists")
    object PlaylistCreate : Screen("playlist_create")
    object PlaylistPlayer : Screen("playlist_player/{playlistId}") {
        fun createRoute(playlistId: String) = "playlist_player/$playlistId"
    }
    object Soundboard : Screen("soundboard")
    object Sync : Screen("sync")
    object Settings : Screen("settings")
}

private val bottomNavScreens = listOf(
    Triple(Screen.Home, Icons.Default.MusicNote, "Home"),
    Triple(Screen.Sync, Icons.Default.Sync, "Sync"),
    Triple(Screen.Settings, Icons.Default.Settings, "Settings")
)

private val routesWithBottomBar = setOf(
    Screen.Home.route,
    Screen.Playlists.route,
    Screen.Soundboard.route,
    Screen.Sync.route,
    Screen.Settings.route
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeaperNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in routesWithBottomBar

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val currentDestination = navBackStackEntry?.destination
                    bottomNavScreens.forEach { (screen, icon, label) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToPlaylists = { navController.navigate(Screen.Playlists.route) },
                    onNavigateToSoundboard = { navController.navigate(Screen.Soundboard.route) },
                    onNavigateToSync = { navController.navigate(Screen.Sync.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.Playlists.route) {
                PlaylistListScreen(
                    onNavigateToCreate = { navController.navigate(Screen.PlaylistCreate.route) },
                    onNavigateToPlayer = { playlistId ->
                        navController.navigate(Screen.PlaylistPlayer.createRoute(playlistId))
                    }
                )
            }

            composable(Screen.PlaylistCreate.route) {
                PlaylistCreationScreen(
                    onNavigateToPlayer = { playlistId ->
                        navController.navigate(Screen.PlaylistPlayer.createRoute(playlistId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.PlaylistPlayer.route,
                arguments = listOf(
                    navArgument("playlistId") { type = NavType.StringType }
                )
            ) {
                PlaylistPlayerScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Soundboard.route) { SoundboardScreen() }
            composable(Screen.Sync.route) { SyncScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
