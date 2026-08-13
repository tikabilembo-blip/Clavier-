package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.KeyboardPreferences
import com.example.ui.screens.AccessibilitySettingsScreen
import com.example.ui.screens.DefaultSetupScreen
import com.example.ui.screens.InteractiveStudioScreen
import com.example.ui.screens.ShortcutsScreen
import com.example.ui.screens.ThemeSelectorScreen
import com.example.ui.theme.MyApplicationTheme

sealed class NavItem(val route: String, val title: String, val icon: ImageVector) {
    object Studio : NavItem("studio", "Essai", Icons.Default.TouchApp)
    object Setup : NavItem("setup", "Activation", Icons.Default.CheckCircle)
    object Themes : NavItem("themes", "Thèmes", Icons.Default.Palette)
    object Accessibility : NavItem("accessibility", "Accès", Icons.Default.AccessibilityNew)
    object Shortcuts : NavItem("shortcuts", "Raccourcis", Icons.Default.VpnKey)
}

class MainActivity : ComponentActivity() {

    private lateinit var preferences: KeyboardPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = KeyboardPreferences.getInstance(this)

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: NavItem.Studio.route

                val navItems = listOf(
                    NavItem.Studio,
                    NavItem.Setup,
                    NavItem.Themes,
                    NavItem.Accessibility,
                    NavItem.Shortcuts
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            navItems.forEach { item ->
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
                                    icon = { Icon(item.icon, contentDescription = item.title) },
                                    label = { Text(item.title) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = NavItem.Studio.route,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        composable(NavItem.Studio.route) {
                            InteractiveStudioScreen(preferences = preferences)
                        }

                        composable(NavItem.Setup.route) {
                            DefaultSetupScreen(
                                onNavigateToStudio = {
                                    navController.navigate(NavItem.Studio.route)
                                }
                            )
                        }

                        composable(NavItem.Themes.route) {
                            ThemeSelectorScreen(preferences = preferences)
                        }

                        composable(NavItem.Accessibility.route) {
                            AccessibilitySettingsScreen(preferences = preferences)
                        }

                        composable(NavItem.Shortcuts.route) {
                            ShortcutsScreen()
                        }
                    }
                }
            }
        }
    }
}
