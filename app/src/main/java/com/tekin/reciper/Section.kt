package com.tekin.reciper

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.tekin.reciper.UserData

class Section {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val usersRef = FirebaseDatabase.getInstance().getReference("users")

    private val _currentUserData = MutableLiveData<UserData?>()
    val currentUserData: LiveData<UserData?> get() = _currentUserData

    init {
        val user = auth.currentUser
        if (user != null) {
            loadUserData(user.uid)
        }
    }

    fun signedIn(): Boolean {
        return auth.currentUser != null
    }

    fun signIn(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                user?.let {
                    loadUserData(it.uid)
                    callback(true)
                }
            } else {
                callback(false)
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUserData.postValue(null)
    }

    fun register(email: String, password: String, userData: UserData, callback: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                user?.let {
                    val userID = it.uid
                    usersRef.child(userID).setValue(userData)
                    callback(true)
                }
            } else {
                callback(false)
            }
        }
    }

    private fun loadUserData(userID: String) {
        usersRef.child(userID).get().addOnSuccessListener { snapshot ->
            val userData = snapshot.getValue(UserData::class.java)
            _currentUserData.postValue(userData)
        }.addOnFailureListener {
            _currentUserData.postValue(null)
        }
    }
}

