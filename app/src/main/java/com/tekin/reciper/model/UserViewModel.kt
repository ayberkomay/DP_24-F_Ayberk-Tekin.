package com.tekin.reciper.model

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.tekin.reciper.Section
import com.tekin.reciper.data.RecipeData
import com.tekin.reciper.data.UserData

class UserViewModel : ViewModel() {
    private val section = Section()
    val currentUserData: LiveData<UserData?> = section.currentUserData

    fun signedIn(): Boolean {
        return section.signedIn()
    }

    fun getCurrentUserID(): String? {
        return section.getCurrentUserID()
    }

    fun signIn(email: String, password: String, callback: (Boolean) -> Unit) {
        section.signIn(email, password, callback)
    }

    fun signOut() {
        section.signOut()
    }

    fun register(email: String, password: String, userData: UserData, callback: (Boolean) -> Unit) {
        section.register(email, password, userData, callback)
    }

    fun updateUserData(updatedFields: Map<String, Any?>, password: String?, callback: (Boolean, String?) -> Unit) {
        section.updateUserData(updatedFields, password, callback)
    }

    fun uploadProfileImageUri(imageUri: Uri, callback: (Boolean, String?) -> Unit) {
        val userID = getCurrentUserID()
        if (userID == null) {
            callback(false, "User not authenticated")
            return
        }
        section.uploadProfileImageUri(userID, imageUri, callback)
    }

    fun getProfileImageUri(callback: (Uri?) -> Unit) {
        val userID = getCurrentUserID()
        if (userID == null) {
            callback(null)
            return
        }
        section.getProfileImageUri(userID, callback)
    }

    fun getProfileImageUriByUserId(userID: String, callback: (Uri?) -> Unit) {
        section.getProfileImageUriByUserId(userID, callback)
    }

    fun generateRecipeId(): String {
        return section.generateRecipeId()
    }

    fun addRecipe(recipeData: RecipeData, callback: (Boolean, String?) -> Unit) {
        section.addRecipe(recipeData, callback)
    }

    fun getUserRecipes(startIndex: Int, limit: Int, callback: (List<RecipeData>) -> Unit) {
        val userID = getCurrentUserID()
        if (userID != null) {
            section.getUserRecipes(userID, startIndex, limit, callback)
        } else {
            callback(emptyList())
        }
    }

    fun getUserRecipesByUserId(userID: String, startIndex: Int, limit: Int, callback: (List<RecipeData>) -> Unit) {
        section.getUserRecipes(userID, startIndex, limit, callback)
    }


    fun uploadRecipeImageUri(userID: String, recipeId: String, imageUri: Uri, callback: (Boolean, String?, String?) -> Unit) {
        section.uploadRecipeImageUri(userID, recipeId, imageUri, callback)
    }

    fun getRecipeById(recipeId: String, callback: (RecipeData?) -> Unit) {
        section.getRecipeById(recipeId, callback)
    }

    fun deleteRecipe(recipeId: String, callback: (Boolean, String?) -> Unit) {
        val userID = getCurrentUserID()
        if (userID != null) {
            section.deleteRecipe(userID, recipeId, callback)
        } else {
            callback(false, "User not authenticated")
        }
    }

    fun updateRecipe(recipeId: String, userID: String, title: String, instructions: String, newImageUri: Uri?, oldImageUrl: String?, callback: (Boolean, String?) -> Unit) {
        section.updateRecipe(recipeId, userID, title, instructions, newImageUri, oldImageUrl, callback)
    }

    fun toggleLikeRecipe(recipeId: String, callback: (Boolean) -> Unit) {
        val userID = getCurrentUserID()
        if (userID == null) {
            callback(false)
        } else {
            section.toggleLikeRecipe(recipeId, userID, callback)
        }
    }

    fun getTopRatedRecipes(startIndex: Int, limit: Int, callback: (List<RecipeData>) -> Unit) {
        section.getTopRatedRecipes(startIndex, limit, callback)
    }

    fun getFavorites(startIndex: Int, limit: Int, callback: (List<RecipeData>) -> Unit) {
        val userID = getCurrentUserID()
        if (userID != null) {
            section.getFavorites(userID, startIndex, limit, callback)
        } else {
            callback(emptyList())
        }
    }

    fun searchRecipes(query: String, startIndex: Int, limit: Int, callback: (List<RecipeData>) -> Unit) {
        section.searchRecipes(query, startIndex, limit, callback)
    }

    fun getUserDataById(userID: String, callback: (UserData?) -> Unit) {
        section.getUserDataById(userID, callback)
    }
}
