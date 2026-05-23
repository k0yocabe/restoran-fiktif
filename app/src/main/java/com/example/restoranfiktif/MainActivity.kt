package com.example.restoranfiktif

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.restoranfiktif.data.PreferenceManager
import com.example.restoranfiktif.ui.Screen
import com.example.restoranfiktif.ui.screens.*
import com.example.restoranfiktif.ui.theme.RestoranFiktifTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferenceManager = PreferenceManager(this)

        setContent {
            var isDarkTheme by remember { mutableStateOf(preferenceManager.isDarkTheme()) }

            RestoranFiktifTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val screenOrder = listOf(
                    Screen.Home.route,
                    Screen.Menu.route,
                    Screen.Cart.route,
                    Screen.Favorite.route,
                    Screen.Profile.route
                )

                val showBottomBar = currentDestination?.route in screenOrder

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                val items = listOf(
                                    Triple(Screen.Home, "Home", Icons.Default.Home),
                                    Triple(Screen.Menu, "Menu", Icons.Default.Menu),
                                    Triple(Screen.Cart, "Keranjang", Icons.Default.ShoppingCart),
                                    Triple(Screen.Favorite, "Favorit", Icons.Default.Favorite),
                                    Triple(Screen.Profile, "Profil", Icons.Default.Person)
                                )
                                items.forEach { (screen, label, icon) ->
                                    NavigationBarItem(
                                        icon = { Icon(icon, contentDescription = label) },
                                        label = { Text(label) },
                                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                        onClick = {
                                            if (currentDestination?.route != screen.route) {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0) // Disable automatic scaffold padding
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Splash.route,
                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                    ) {
                        composable(
                            route = Screen.Splash.route,
                            enterTransition = { fadeIn() },
                            exitTransition = { fadeOut() }
                        ) {
                            SplashScreen(navController)
                        }

                        // Main Screens
                        screenOrder.forEach { route ->
                            composable(
                                route = route,
                                enterTransition = {
                                    val initialState = initialState.destination.route
                                    val targetState = targetState.destination.route
                                    val initialIndex = screenOrder.indexOf(initialState)
                                    val targetIndex = screenOrder.indexOf(targetState)
                                    
                                    if (initialIndex != -1 && targetIndex != -1) {
                                        if (targetIndex > initialIndex) {
                                            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) + fadeIn(tween(400))
                                        } else {
                                            slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(400)) + fadeIn(tween(400))
                                        }
                                    } else {
                                        fadeIn(tween(400))
                                    }
                                },
                                exitTransition = {
                                    val initialState = initialState.destination.route
                                    val targetState = targetState.destination.route
                                    val initialIndex = screenOrder.indexOf(initialState)
                                    val targetIndex = screenOrder.indexOf(targetState)
                                    
                                    if (initialIndex != -1 && targetIndex != -1) {
                                        if (targetIndex > initialIndex) {
                                            slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400)) + fadeOut(tween(400))
                                        } else {
                                            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) + fadeOut(tween(400))
                                        }
                                    } else {
                                        fadeOut(tween(400))
                                    }
                                }
                            ) {
                                when (route) {
                                    Screen.Home.route -> HomeScreen(navController, preferenceManager)
                                    Screen.Menu.route -> MenuScreen(navController, preferenceManager)
                                    Screen.Cart.route -> CartScreen(navController, preferenceManager)
                                    Screen.Favorite.route -> FavoriteScreen(navController, preferenceManager)
                                    Screen.Profile.route -> ProfileScreen(navController, preferenceManager, onThemeChange = { 
                                        isDarkTheme = it
                                        preferenceManager.saveTheme(it)
                                    })
                                }
                            }
                        }

                        composable(
                            route = Screen.DetailMenu.route,
                            arguments = listOf(navArgument("menuId") { type = NavType.IntType }),
                            enterTransition = {
                                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) + fadeIn(tween(400))
                            },
                            exitTransition = {
                                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) + fadeOut(tween(400))
                            }
                        ) { backStackEntry ->
                            val menuId = backStackEntry.arguments?.getInt("menuId") ?: 0
                            DetailMenuScreen(navController, menuId, preferenceManager)
                        }
                        
                        composable(
                            route = Screen.EditProfile.route,
                            enterTransition = {
                                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) + fadeIn(tween(400))
                            },
                            exitTransition = {
                                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) + fadeOut(tween(400))
                            }
                        ) {
                            EditProfileScreen(navController, preferenceManager)
                        }
                    }
                }
            }
        }
    }
}
