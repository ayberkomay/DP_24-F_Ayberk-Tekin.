package com.tekin.reciper

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.tekin.reciper.databinding.FragmentSettingsBinding
// txt dosyasındaki gibi değiştirebilirim!!!!!
// It works slow / not stable
// New Register fragment????
// Some issues about date and phone number texting.

/* I'll add,
*  update informations settings,
*  personal informations,
*  profile photo
*  my receipts area?*/
class Settings : Fragment(R.layout.fragment_settings) {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentSettingsBinding
    private var backToRegisterOnce: Boolean = false // Add this variable

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        val usersRef = FirebaseDatabase.getInstance().getReference("users")

        val signedInLayout = binding.layoutSignedIn
        val signedOutLayout = binding.layoutSignedOut
        val registerLayout = binding.layoutRegister

        if (user != null) {
            // User signed in
            signedInLayout.visibility = View.VISIBLE
            signedOutLayout.visibility = View.GONE
            registerLayout.visibility = View.GONE

            binding.textUserEmail.text = user.email

            binding.buttonSignout.setOnClickListener {
                auth.signOut()
                // Signed out
                signedInLayout.visibility = View.GONE
                signedOutLayout.visibility = View.VISIBLE
                registerLayout.visibility = View.GONE
            }

        } else {
            // Not signed in
            signedInLayout.visibility = View.GONE
            signedOutLayout.visibility = View.VISIBLE
            registerLayout.visibility = View.GONE

            val btnSignin = binding.buttonSignin
            val btnRegisterSender = binding.buttonRegisterSender

            // Sign in button
            btnSignin.setOnClickListener {
                val email = binding.textMail.text.toString().trim()
                val password = binding.textPassword.text.toString().trim()

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { signin ->
                        if (signin.isSuccessful) {
                            val currentUser = auth.currentUser
                            currentUser?.let {
                                Toast.makeText(context, "Successful", Toast.LENGTH_LONG).show()
                                // Signed in
                                signedInLayout.visibility = View.VISIBLE
                                signedOutLayout.visibility = View.GONE
                                registerLayout.visibility = View.GONE

                                binding.textUserEmail.text = currentUser.email
                            }
                        } else {
                            val errorMessage = signin.exception?.message
                            Toast.makeText(context, "$errorMessage", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_LONG).show()
                }
            }

            // Register sender
            btnRegisterSender.setOnClickListener {
                backToRegisterOnce = true
                // Show register screen
                signedInLayout.visibility = View.GONE
                signedOutLayout.visibility = View.GONE
                registerLayout.visibility = View.VISIBLE

                val btnRegister = binding.buttonRegister
                val btnRegisterBack = binding.buttonRegisterBack

                btnRegister.setOnClickListener {

                    val email = binding.textRegisterMail.text.toString().trim()
                    val password = binding.textRegisterPassword.text.toString().trim()
                    val name = binding.textRegisterName.text.toString().trim()
                    val surname = binding.textRegisterSurname.text.toString().trim()
                    val phone = binding.textRegisterPhone.text.toString().trim()
                    val date = binding.editTextRegisterBirthDate.text.toString().trim()

                    if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() && surname.isNotEmpty() && phone.isNotEmpty() && date.isNotEmpty()) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { register ->
                                if (register.isSuccessful) {
                                    val newUser = auth.currentUser
                                    newUser?.let {
                                        // Successful registration, get unique uid
                                        val userId = it.uid
                                        val userData = UserData(email, name, surname, phone, date)
                                        // Save user data to database with uid
                                        usersRef.child(userId).setValue(userData)
                                        Toast.makeText(context, "Successful", Toast.LENGTH_LONG).show()
                                        signedInLayout.visibility = View.VISIBLE
                                        signedOutLayout.visibility = View.GONE
                                        registerLayout.visibility = View.GONE

                                        binding.textUserEmail.text = newUser.email
                                    }
                                } else {
                                    val errorMessage = register.exception?.message
                                    Toast.makeText(context, "$errorMessage", Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_LONG).show()
                    }
                }

                btnRegisterBack.setOnClickListener {
                    // Go back to sign-in screen
                    signedInLayout.visibility = View.GONE
                    signedOutLayout.visibility = View.VISIBLE
                    registerLayout.visibility = View.GONE
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    registerLayout.visibility == View.VISIBLE -> {
                        // If registerLayout is visible, go back to signedOutLayout
                        registerLayout.visibility = View.GONE
                        signedOutLayout.visibility = View.VISIBLE
                        signedInLayout.visibility = View.GONE
                    }
                    signedOutLayout.visibility == View.VISIBLE -> {
                        if (backToRegisterOnce) {
                            // Go back to register view once
                            registerLayout.visibility = View.VISIBLE
                            signedOutLayout.visibility = View.GONE
                            signedInLayout.visibility = View.GONE
                            backToRegisterOnce = false
                        } else {
                            isEnabled = false
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    }
                    else -> {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })
    }
}
