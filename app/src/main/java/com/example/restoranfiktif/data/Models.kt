package com.example.restoranfiktif.data

import androidx.annotation.DrawableRes

data class MenuItem(
    val id: Int,
    val name: String,
    val price: String,
    val description: String,
    @DrawableRes val imageRes: Int,
    val rating: Double = 0.0,
    val category: String = "Lainnya"
)

data class RestaurantProfile(
    val name: String,
    val address: String,
    val description: String,
    val openingHours: String
)
