package id.app.rplayer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.app.rplayer.LocalAppContainer
import id.app.rplayer.ui.component.MiniPlayer
import id.app.rplayer.ui.screen.HomeScreen
import id.app.rplayer.ui.screen.LibraryScreen
import id.app.rplayer.ui.screen.PlaylistDetailScreen
import id.app.rplayer.ui.screen.PlayerScreen
import id.app.rplayer.ui.screen.SearchScreen
import id.app.rplayer.ui.screen.SettingsScreen

private data class Destination(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun AppRoot() {
    val container = LocalAppContainer.current
    val navController: NavHostController = rememberNavController()
    val playerViewModel: PlayerViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = PlayerViewModel(container.player) as T
        },
    )
    val playerState by playerViewModel.state.collectAsStateWithLifecycle()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val destinations = listOf(
        Destination("home", "Beranda", Icons.Filled.Home),
        Destination("search", "Cari", Icons.Filled.Search),
        Destination("library", "Pustaka", Icons.Filled.LibraryMusic),
        Destination("settings", "Setelan", Icons.Filled.Settings),
    )

    Scaffold(
        bottomBar = {
            if (currentRoute != "player") {
                Column {
                    if (playerState.hasQueue) {
                        MiniPlayer(
                            state = playerState,
                            playerViewModel = playerViewModel,
                            onOpen = { navController.navigate("player") },
                        )
                    }
                    NavigationBar {
                        destinations.forEach { destination ->
                            NavigationBarItem(
                                selected = currentRoute == destination.route,
                                onClick = {
                                    navController.navigate(destination.route) {
                                        popUpTo("home") { inclusive = false }
                                        launchSingleTop = true
                                    }
                                },
                                icon = { Icon(destination.icon, contentDescription = null) },
                                label = { Text(destination.label) },
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding),
        ) {
            composable("home") { HomeScreen(container.repository, playerViewModel) }
            composable("search") { SearchScreen(container.repository, playerViewModel) }
            composable("library") {
                LibraryScreen(
                    repository = container.repository,
                    playerViewModel = playerViewModel,
                    onOpenPlaylist = { id -> navController.navigate("playlist/$id") },
                )
            }
            composable("playlist/{playlistId}") { entry ->
                val id = entry.arguments?.getString("playlistId")?.toLongOrNull() ?: 0L
                PlaylistDetailScreen(
                    playlistId = id,
                    repository = container.repository,
                    playerViewModel = playerViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
            composable("player") {
                PlayerScreen(playerViewModel = playerViewModel, onBack = { navController.popBackStack() })
            }
            composable("settings") { SettingsScreen(container.repository) }
        }
    }
}
