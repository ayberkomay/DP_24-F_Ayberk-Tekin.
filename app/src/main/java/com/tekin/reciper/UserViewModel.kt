package com.tekin.reciper

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

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

    fun uploadProfileImage(imageData: ByteArray, callback: (Boolean, String?) -> Unit) {
        val userID = getCurrentUserID()
        if (userID == null) {
            callback(false, "User not authenticated")
            return
        }
        section.uploadProfileImage(userID, imageData, callback)
    }

    fun getProfileImageUri(callback: (Uri?) -> Unit) {
        val userID = getCurrentUserID()
        if (userID == null) {
            callback(null)
            return
        }
        section.getProfileImageUri(userID, callback)
    }
}
