package com.example.restoranfiktif.data

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("restoran_fiktif_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_RESTAURANT_NAME = "restaurant_name"
        private const val KEY_RESTAURANT_ADDRESS = "restaurant_address"
        private const val KEY_RESTAURANT_DESC = "restaurant_desc"
        private const val KEY_RESTAURANT_HOURS = "restaurant_hours"
        private const val KEY_IS_DARK_THEME = "is_dark_theme"
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_CART = "cart_json"
    }

    fun toggleFavorite(menuId: Int) {
        val favorites = getFavorites().toMutableSet()
        if (favorites.contains(menuId.toString())) {
            favorites.remove(menuId.toString())
        } else {
            favorites.add(menuId.toString())
        }
        sharedPreferences.edit().putStringSet(KEY_FAVORITES, favorites).apply()
    }

    fun getFavorites(): Set<String> {
        return sharedPreferences.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun isFavorite(menuId: Int): Boolean {
        return getFavorites().contains(menuId.toString())
    }

    // Cart Management
    fun addToCart(menuId: Int) {
        val cart = getCart().toMutableMap()
        val currentQty = cart[menuId.toString()] ?: 0
        cart[menuId.toString()] = currentQty + 1
        saveCart(cart)
    }

    fun removeFromCart(menuId: Int) {
        val cart = getCart().toMutableMap()
        val currentQty = cart[menuId.toString()] ?: 0
        if (currentQty > 1) {
            cart[menuId.toString()] = currentQty - 1
        } else {
            cart.remove(menuId.toString())
        }
        saveCart(cart)
    }

    fun deleteFromCart(menuId: Int) {
        val cart = getCart().toMutableMap()
        cart.remove(menuId.toString())
        saveCart(cart)
    }

    private fun saveCart(cart: Map<String, Int>) {
        // Simple string serialization: "id:qty,id:qty"
        val cartString = cart.entries.joinToString(",") { "${it.key}:${it.value}" }
        sharedPreferences.edit().putString(KEY_CART, cartString).apply()
    }

    fun getCart(): Map<String, Int> {
        val cartString = sharedPreferences.getString(KEY_CART, "") ?: ""
        if (cartString.isEmpty()) return emptyMap()
        return cartString.split(",").associate {
            val parts = it.split(":")
            parts[0] to parts[1].toInt()
        }
    }

    fun getItemQuantity(menuId: Int): Int {
        return getCart()[menuId.toString()] ?: 0
    }

    fun saveProfile(profile: RestaurantProfile) {
        sharedPreferences.edit().apply {
            putString(KEY_RESTAURANT_NAME, profile.name)
            putString(KEY_RESTAURANT_ADDRESS, profile.address)
            putString(KEY_RESTAURANT_DESC, profile.description)
            putString(KEY_RESTAURANT_HOURS, profile.openingHours)
            apply()
        }
    }

    fun getProfile(): RestaurantProfile {
        return RestaurantProfile(
            name = sharedPreferences.getString(KEY_RESTAURANT_NAME, "Restoran Fiktif") ?: "Restoran Fiktif",
            address = sharedPreferences.getString(KEY_RESTAURANT_ADDRESS, "Jl. Imajinasi No. 123") ?: "Jl. Imajinasi No. 123",
            description = sharedPreferences.getString(KEY_RESTAURANT_DESC, "Restoran terbaik di dunia imajinasi.") ?: "Restoran terbaik di dunia imajinasi.",
            openingHours = sharedPreferences.getString(KEY_RESTAURANT_HOURS, "09:00 - 21:00") ?: "09:00 - 21:00"
        )
    }

    fun saveTheme(isDark: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_DARK_THEME, isDark).apply()
    }

    fun isDarkTheme(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_DARK_THEME, false)
    }
}
