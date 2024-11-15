package com.tekin.reciper

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.tekin.reciper.UserData
import com.tekin.reciper.Section

class UserViewModel : ViewModel() {
    private val section = Section()
    val currentUserData: LiveData<UserData?> = section.currentUserData

    fun signedIn(): Boolean {
        return section.signedIn()
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
}

