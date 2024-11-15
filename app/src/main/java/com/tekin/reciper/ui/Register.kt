package com.tekin.reciper.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.tekin.reciper.R
import com.tekin.reciper.UserData
import com.tekin.reciper.UserViewModel
import com.tekin.reciper.databinding.FragmentRegisterBinding
import com.tekin.reciper.ui.User.UserNotSignedIn
import com.tekin.reciper.ui.User.UserSignedIn

// Some issues about date and phone number texting.

class Register : Fragment(R.layout.fragment_register){
    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: UserViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegisterBinding.bind(view)

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

                val userData = UserData(email, name, surname, phone, date)
                viewModel.register(email, password, userData) { success ->
                    if (success) {
                        Toast.makeText(context, "Registration successful!", Toast.LENGTH_LONG).show()
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.frame_layout, UserSignedIn())
                            .addToBackStack(null)
                            .commit()
                    } else {
                        Toast.makeText(context, "Registration failed.", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_LONG).show()
            }
        }
        btnRegisterBack.setOnClickListener{
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, UserNotSignedIn())
                .addToBackStack(null)
                .commit()
        }


    }
}
