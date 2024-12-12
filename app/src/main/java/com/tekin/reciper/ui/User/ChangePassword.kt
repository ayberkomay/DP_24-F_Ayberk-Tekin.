package com.tekin.reciper.ui.User

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.tekin.reciper.R
import com.tekin.reciper.databinding.FragmentChangepasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePassword : Fragment(R.layout.fragment_changepassword) {
    private lateinit var binding: FragmentChangepasswordBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentChangepasswordBinding.bind(view)

        val oldPasswordEditText = binding.oldPasswordEditText
        val newPasswordEditText = binding.newPasswordEditText
        val confirmPasswordEditText = binding.confirmPasswordEditText
        val changePasswordButton = binding.changePasswordButton
        val backButton = binding.backButton

        changePasswordButton.setOnClickListener {
            val oldPassword = oldPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (newPassword != confirmPassword) {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = auth.currentUser
            if (user != null && user.email != null) {
                val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
                user.reauthenticate(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Toast.makeText(context, "Password updated", Toast.LENGTH_SHORT).show()
                                parentFragmentManager.popBackStack()
                            } else {
                                Toast.makeText(context, "Error updating password", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            }
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
