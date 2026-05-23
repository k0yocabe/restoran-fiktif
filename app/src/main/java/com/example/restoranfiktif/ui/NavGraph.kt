package com.example.restoranfiktif.ui

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Menu : Screen("menu")
    object Cart : Screen("cart")
    object Favorite : Screen("favorite")
    object DetailMenu : Screen("detail/{menuId}") {
        fun createRoute(menuId: Int) = "detail/$menuId"
    }
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
}
