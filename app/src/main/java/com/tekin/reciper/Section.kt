package com.tekin.reciper

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class Section {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val usersRef = FirebaseDatabase.getInstance().getReference("users")
    private val storageRef = FirebaseStorage.getInstance("gs://reciper-9eba7.firebasestorage.app")

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

    fun getCurrentUserID(): String? {
        return auth.currentUser?.uid
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
                    usersRef.child(userID).setValue(userData).addOnCompleteListener { dbTask ->
                        callback(dbTask.isSuccessful)
                    }
                }
            } else {
                callback(false)
            }
        }
    }

    fun updateUserData(updatedFields: Map<String, Any?>, password: String?, callback: (Boolean, String?) -> Unit) {
        val userID = auth.currentUser?.uid
        val user = auth.currentUser
        if (userID != null && user != null) {
            val databaseUpdates = updatedFields.toMutableMap()

            if (updatedFields.containsKey("email")) {
                val newEmail = updatedFields["email"] as String

                if (password != null && password.isNotEmpty() && user.email != null) {
                    val credential = EmailAuthProvider.getCredential(user.email!!, password)
                    user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            user.updateEmail(newEmail).addOnCompleteListener { emailUpdateTask ->
                                if (emailUpdateTask.isSuccessful) {
                                    usersRef.child(userID).updateChildren(databaseUpdates).addOnCompleteListener { dbUpdateTask ->
                                        if (dbUpdateTask.isSuccessful) {
                                            loadUserData(userID)
                                            callback(true, null)
                                        } else {
                                            val errorMsg = dbUpdateTask.exception?.message ?: "Database update failed"
                                            callback(false, errorMsg)
                                        }
                                    }
                                } else {
                                    val errorMsg = emailUpdateTask.exception?.message ?: "Email update failed"
                                    callback(false, errorMsg)
                                }
                            }
                        } else {
                            val errorMsg = reauthTask.exception?.message ?: "Re-authentication failed"
                            callback(false, errorMsg)
                        }
                    }
                } else {
                    callback(false, "Password is required to update email")
                }
            } else {
                usersRef.child(userID).updateChildren(databaseUpdates).addOnCompleteListener { dbUpdateTask ->
                    if (dbUpdateTask.isSuccessful) {
                        loadUserData(userID)
                        callback(true, null)
                    } else {
                        val errorMsg = dbUpdateTask.exception?.message ?: "Database update failed"
                        callback(false, errorMsg)
                    }
                }
            }
        } else {
            callback(false, "User is not authenticated")
        }
    }

    fun uploadProfileImage(userID: String, imageData: ByteArray, callback: (Boolean, String?) -> Unit) {
        val imageRef = storageRef.reference.child("profileImages/$userID/profile")
        val uploadTask = imageRef.putBytes(imageData)
        uploadTask.addOnFailureListener {
            callback(false, it.message)
        }.addOnSuccessListener {
            callback(true, null)
        }
    }

    fun getProfileImageUri(userID: String, callback: (Uri?) -> Unit) {
        val imageRef = storageRef.reference.child("profileImages/$userID/profile")
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            callback(uri)
        }.addOnFailureListener {
            callback(null)
        }
    }

    private fun loadUserData(userID: String) {
        usersRef.child(userID).addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val userData = snapshot.getValue(UserData::class.java)
                _currentUserData.postValue(userData)
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                _currentUserData.postValue(null)
            }
        })
    }
}
