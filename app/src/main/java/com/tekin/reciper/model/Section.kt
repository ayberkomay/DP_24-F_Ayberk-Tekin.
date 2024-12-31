package com.tekin.reciper

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.tekin.reciper.data.RecipeData
import com.tekin.reciper.data.UserData
import android.os.Handler
import android.os.Looper

class Section {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance("https://reciper-9eba7-default-rtdb.europe-west1.firebasedatabase.app/")
    private val usersRef = db.getReference("users")
    private val recipesRef = db.getReference("recipes")
    private val userFavoritesRef = db.getReference("userFavorites")
    private val storage = FirebaseStorage.getInstance("gs://reciper-9eba7.firebasestorage.app")
    private val profileImagesRef = storage.getReference("profileImages")
    private val recipesImagesRef = storage.getReference("recipesImages")
    private val _currentUserData = MutableLiveData<UserData?>()
    val currentUserData: LiveData<UserData?> get() = _currentUserData

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            loadUserData(user.uid) {}
        } else {
            _currentUserData.postValue(null)
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
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
                callback(true)
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
                if (user != null) {
                    val userID = user.uid
                    usersRef.child(userID).setValue(userData).addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            loadUserData(userID) {
                                callback(true)
                            }
                        } else {
                            callback(false)
                        }
                    }
                } else {
                    callback(false)
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
            if (updatedFields.containsKey("email")) {
                val newEmail = updatedFields["email"] as String
                if (password != null && password.isNotEmpty() && user.email != null) {
                    val credential = EmailAuthProvider.getCredential(user.email!!, password)
                    user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            user.updateEmail(newEmail).addOnCompleteListener { emailUpdateTask ->
                                if (emailUpdateTask.isSuccessful) {
                                    updateUserFields(userID, updatedFields, callback)
                                } else {
                                    callback(false, emailUpdateTask.exception?.message)
                                }
                            }
                        } else {
                            callback(false, reauthTask.exception?.message)
                        }
                    }
                } else {
                    callback(false, "Password is required to update email")
                }
            } else {
                updateUserFields(userID, updatedFields, callback)
            }
        } else {
            callback(false, "User is not authenticated")
        }
    }

    private fun updateUserFields(userID: String, updatedFields: Map<String, Any?>, callback: (Boolean, String?) -> Unit) {
        usersRef.child(userID).updateChildren(updatedFields).addOnCompleteListener { dbUpdateTask ->
            if (dbUpdateTask.isSuccessful) {
                loadUserData(userID) {
                    callback(true, null)
                }
            } else {
                callback(false, dbUpdateTask.exception?.message)
            }
        }
    }

    fun generateRecipeId(): String {
        return recipesRef.push().key ?: ""
    }

    fun addRecipe(recipeData: RecipeData, callback: (Boolean, String?) -> Unit) {
        if (recipeData.recipeId != null && recipeData.recipeId.isNotEmpty()) {
            recipesRef.child(recipeData.recipeId).setValue(recipeData).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
        } else {
            callback(false, "Invalid recipeId")
        }
    }

    fun getUserRecipes(userID: String, startIndex: Int, limit: Int, callback: (List<RecipeData>) -> Unit) {
        val query = recipesRef.orderByChild("userID").equalTo(userID)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recipeList = mutableListOf<RecipeData>()
                for (child in snapshot.children) {
                    val recipe = child.getValue(RecipeData::class.java)
                    if (recipe != null) {
                        recipeList.add(recipe)
                    }
                }
                val paginatedList = recipeList.drop(startIndex).take(limit)
                callback(paginatedList)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

    fun uploadProfileImageUri(userID: String, imageUri: Uri, callback: (Boolean, String?) -> Unit) {
        val imageRef = profileImagesRef.child("$userID/profile.jpg")
        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener {
                    callback(true, null)
                }.addOnFailureListener {
                    callback(false, it.message)
                }
            }
            .addOnFailureListener {
                callback(false, it.message)
            }
    }

    fun uploadRecipeImageUri(userID: String, recipeId: String, imageUri: Uri, callback: (Boolean, String?, String?) -> Unit) {
        val imageRef = recipesImagesRef.child("$userID/$recipeId.jpg")
        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    callback(true, uri.toString(), null)
                }.addOnFailureListener {
                    callback(false, null, it.message)
                }
            }
            .addOnFailureListener {
                callback(false, null, it.message)
            }
    }

    fun getProfileImageUri(userID: String, callback: (Uri?) -> Unit) {
        val imageRef = profileImagesRef.child("$userID/profile.jpg")
        imageRef.downloadUrl.addOnSuccessListener {
            callback(it)
        }.addOnFailureListener {
            callback(null)
        }
    }

    fun getProfileImageUriByUserId(userID: String, callback: (Uri?) -> Unit) {
        val imageRef = profileImagesRef.child("$userID/profile.jpg")
        imageRef.downloadUrl.addOnSuccessListener {
            callback(it)
        }.addOnFailureListener {
            callback(null)
        }
    }

    fun getRecipeById(recipeId: String, callback: (RecipeData?) -> Unit) {
        recipesRef.child(recipeId).get().addOnSuccessListener { snapshot ->
            val recipe = snapshot.getValue(RecipeData::class.java)
            callback(recipe)
        }.addOnFailureListener {
            callback(null)
        }
    }

    fun deleteRecipe(userID: String, recipeId: String, callback: (Boolean, String?) -> Unit) {
        val imageRef = recipesImagesRef.child("$userID/$recipeId.jpg")
        recipesRef.child(recipeId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                imageRef.delete().addOnCompleteListener { imgTask ->
                    if (imgTask.isSuccessful) {
                        callback(true, null)
                    } else {
                        callback(false, imgTask.exception?.message)
                    }
                }
            } else {
                callback(false, task.exception?.message)
            }
        }
    }

    fun updateRecipe(recipeId: String, userID: String, title: String, instructions: String, newImageUri: Uri?, oldImageUrl: String?, callback: (Boolean, String?) -> Unit) {
        fun updateDatabase(imageUrl: String) {
            val updatedRecipe = mapOf<String, Any?>(
                "title" to title,
                "instructions" to instructions,
                "imageUrl" to imageUrl
            )
            recipesRef.child(recipeId).updateChildren(updatedRecipe).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
        }
        if (newImageUri != null) {
            val imageRef = recipesImagesRef.child("$userID/$recipeId.jpg")
            imageRef.putFile(newImageUri).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener {
                    updateDatabase(it.toString())
                }.addOnFailureListener {
                    callback(false, it.message)
                }
            }.addOnFailureListener {
                callback(false, it.message)
            }
        } else {
            val imageUrl = oldImageUrl ?: ""
            updateDatabase(imageUrl)
        }
    }

    fun toggleLikeRecipe(recipeId: String, userID: String, callback: (Boolean) -> Unit) {
        userFavoritesRef.child(userID).child(recipeId).get().addOnSuccessListener { snap ->
            val alreadyLiked = snap.exists()
            if (alreadyLiked) {
                recipesRef.child(recipeId).get().addOnSuccessListener { rSnap ->
                    val recipe = rSnap.getValue(RecipeData::class.java)
                    if (recipe != null && recipe.likes > 0) {
                        val newLikes = recipe.likes - 1
                        val updates = mapOf("likes" to newLikes)
                        recipesRef.child(recipeId).updateChildren(updates).addOnSuccessListener {
                            userFavoritesRef.child(userID).child(recipeId).removeValue().addOnSuccessListener {
                                callback(false)
                            }.addOnFailureListener {
                                callback(false)
                            }
                        }.addOnFailureListener {
                            callback(false)
                        }
                    } else {
                        callback(false)
                    }
                }.addOnFailureListener {
                    callback(false)
                }
            } else {
                recipesRef.child(recipeId).get().addOnSuccessListener { rSnap ->
                    val recipe = rSnap.getValue(RecipeData::class.java)
                    if (recipe != null) {
                        val newLikes = recipe.likes + 1
                        val updates = mapOf("likes" to newLikes)
                        recipesRef.child(recipeId).updateChildren(updates).addOnSuccessListener {
                            userFavoritesRef.child(userID).child(recipeId).setValue(true).addOnSuccessListener {
                                callback(true)
                            }.addOnFailureListener {
                                callback(false)
                            }
                        }.addOnFailureListener {
                            callback(false)
                        }
                    } else {
                        callback(false)
                    }
                }.addOnFailureListener {
                    callback(false)
                }
            }
        }.addOnFailureListener {
            callback(false)
        }
    }

    fun getTopRatedRecipes(startIndex: Int, limit: Int, callback: (List<RecipeData>) -> Unit) {
        recipesRef.get().addOnSuccessListener { snapshot ->
            val list = mutableListOf<RecipeData>()
            for (child in snapshot.children) {
                val recipe = child.getValue(RecipeData::class.java)
                if (recipe != null) {
                    list.add(recipe)
                }
            }
            val sorted = list.sortedByDescending { it.likes }
            val paginatedList = sorted.drop(startIndex).take(limit)
            callback(paginatedList)
        }.addOnFailureListener {
            callback(emptyList())
        }
    }

    fun getFavorites(userID: String, startIndex: Int, limit: Int, callback: (List<RecipeData>) -> Unit) {
        userFavoritesRef.child(userID).get().addOnSuccessListener { favSnap ->
            val favorites = mutableListOf<String>()
            for (child in favSnap.children) {
                favorites.add(child.key.toString())
            }
            if (favorites.isEmpty()) {
                callback(emptyList())
            } else {
                recipesRef.get().addOnSuccessListener { snap ->
                    val list = mutableListOf<RecipeData>()
                    for (child in snap.children) {
                        val recipe = child.getValue(RecipeData::class.java)
                        if (recipe != null && favorites.contains(recipe.recipeId)) {
                            list.add(recipe)
                        }
                    }
                    val paginatedList = list.drop(startIndex).take(limit)
                    callback(paginatedList)
                }.addOnFailureListener {
                    callback(emptyList())
                }
            }
        }.addOnFailureListener {
            callback(emptyList())
        }
    }

    fun searchRecipes(query: String, startIndex: Int, limit: Int, callback: (List<RecipeData>) -> Unit) {
        recipesRef.get().addOnSuccessListener { snapshot ->
            val list = mutableListOf<RecipeData>()
            for (child in snapshot.children) {
                val recipe = child.getValue(RecipeData::class.java)
                if (recipe != null && recipe.title != null && recipe.title.contains(query, ignoreCase = true)) {
                    list.add(recipe)
                }
            }
            val paginatedList = list.drop(startIndex).take(limit)
            callback(paginatedList)
        }.addOnFailureListener {
            callback(emptyList())
        }
    }

    fun getUserDataById(userID: String, callback: (UserData?) -> Unit) {
        usersRef.child(userID).get().addOnSuccessListener { snap ->
            val userData = snap.getValue(UserData::class.java)
            callback(userData)
        }.addOnFailureListener {
            callback(null)
        }
    }

    private fun loadUserData(userID: String, callback: () -> Unit) {
        usersRef.child(userID).get().addOnSuccessListener { snapshot ->
            val userData = snapshot.getValue(UserData::class.java)
            if (userData != null) {
                _currentUserData.postValue(userData)
            } else {
                _currentUserData.postValue(null)
            }
            callback()
        }.addOnFailureListener {
            _currentUserData.postValue(null)
            callback()
        }
    }

    fun cleanup() {
        auth.removeAuthStateListener(authStateListener)
    }
}
