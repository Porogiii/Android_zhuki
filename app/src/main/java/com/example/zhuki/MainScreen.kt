package com.example.zhuki

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.painterResource
import com.example.zhuki.model.GameSettings
import com.example.zhuki.model.Player
import com.example.zhuki.ui.screens.AuthorsScreen
import com.example.zhuki.ui.screens.PlayerFormScreen
import com.example.zhuki.ui.screens.RulesScreen
import com.example.zhuki.ui.screens.SettingsScreen
import com.example.zhuki.R

sealed class Screen(val title: String, val iconResId: Int) {
    object PlayerForm : Screen("Игрок", R.drawable.ic_player)
    object Rules : Screen("Правила", R.drawable.ic_rules)
    object Authors : Screen("Авторы", R.drawable.ic_authors)
    object Settings : Screen("Настройки", R.drawable.ic_settings)
}

@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.PlayerForm) }
    var player by remember { mutableStateOf(Player()) }
    var gameSettings by remember { mutableStateOf(GameSettings()) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(
                    Screen.PlayerForm,
                    Screen.Rules,
                    Screen.Authors,
                    Screen.Settings
                ).forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(id = screen.iconResId),
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            is Screen.PlayerForm -> PlayerFormScreen(
                player = player,
                onPlayerUpdate = { player = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
            is Screen.Rules -> RulesScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
            is Screen.Authors -> AuthorsScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
            is Screen.Settings -> SettingsScreen(
                gameSettings = gameSettings,
                onSettingsUpdate = { gameSettings = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}