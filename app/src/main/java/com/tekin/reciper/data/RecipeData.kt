package com.tekin.reciper.data

data class RecipeData(
    val recipeId: String? = null,
    val userID: String? = null,
    val title: String? = null,
    val instructions: String? = null,
    val imageUrl: String? = null,
    val likes: Int = 0
)
