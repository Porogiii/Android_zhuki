package com.example.zhuki

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.zhuki.model.GameSettings
import com.example.zhuki.model.Player
import com.example.zhuki.ui.screens.AuthorsScreen
import com.example.zhuki.ui.screens.GameScreen
import com.example.zhuki.ui.screens.PlayerFormScreen
import com.example.zhuki.ui.screens.RulesScreen
import com.example.zhuki.ui.screens.SettingsScreen

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    var gameSettings by remember { mutableStateOf(GameSettings()) }
    var player by remember { mutableStateOf(Player()) }

    val items = listOf(
        BottomItem("game", "Игра", Icons.Filled.Home),
        BottomItem("rules", "Правила", Icons.Filled.Info),
        BottomItem("authors", "Авторы", Icons.Filled.People),
        BottomItem("settings", "Настройки", Icons.Filled.Settings),
        BottomItem("profile", "Анкета", Icons.Filled.Person)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(tonalElevation = 0.dp) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "profile",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("game") { GameScreen(settings = gameSettings) }
            composable("rules") { RulesScreen() }
            composable("authors") { AuthorsScreen() }
            composable("settings") {
                SettingsScreen(
                    gameSettings = gameSettings,
                    onSettingsUpdate = { gameSettings = it }
                )
            }
            composable("profile") {
                PlayerFormScreen(
                    player = player,
                    onPlayerUpdate = { player = it }
                )
            }
        }
    }
}
