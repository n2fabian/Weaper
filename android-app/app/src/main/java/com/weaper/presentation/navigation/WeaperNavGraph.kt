package com.weaper.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.weaper.presentation.setlist.SetlistScreen
import com.weaper.presentation.settings.SettingsScreen
import com.weaper.presentation.soundboard.SoundboardScreen
import com.weaper.presentation.sync.SyncScreen

sealed class Screen(val route: String, val label: String) {
    object Setlist : Screen("setlist", "Setlist")
    object Soundboard : Screen("soundboard", "Soundboard")
    object Sync : Screen("sync", "Sync")
    object Settings : Screen("settings", "Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeaperNavGraph() {
    val navController = rememberNavController()

    val items = listOf(
        Triple(Screen.Setlist, Icons.Default.List, "Setlist"),
        Triple(Screen.Soundboard, Icons.Default.MusicNote, "Soundboard"),
        Triple(Screen.Sync, Icons.Default.Sync, "Sync"),
        Triple(Screen.Settings, Icons.Default.Settings, "Settings")
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { (screen, icon, label) ->
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Setlist.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Setlist.route) { SetlistScreen() }
            composable(Screen.Soundboard.route) { SoundboardScreen() }
            composable(Screen.Sync.route) { SyncScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
