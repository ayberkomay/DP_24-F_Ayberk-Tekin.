package com.tekin.reciper.ui.User

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.tekin.reciper.R
import com.tekin.reciper.model.UserViewModel
import com.tekin.reciper.databinding.FragmentUserNotSignedinBinding
import com.tekin.reciper.ui.User.others.Register

class UserNotSignedIn : Fragment(R.layout.fragment_user_not_signedin) {
    private lateinit var binding: FragmentUserNotSignedinBinding
    private val viewModel: UserViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserNotSignedinBinding.bind(view)

        val btnSignin = binding.buttonSignin
        val btnRegisterSender = binding.buttonRegisterSender

        btnSignin.setOnClickListener {
            val email = binding.textMail.text.toString().trim()
            val password = binding.textPassword.text.toString().trim()

            if(email.isNotEmpty() && password.isNotEmpty()){
                viewModel.signIn(email, password) { signin ->
                    if(signin){
                        Toast.makeText(context, "Successful", Toast.LENGTH_LONG).show()
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.frame_layout, UserSignedIn())
                            .commit()
                    }else{
                        Toast.makeText(context, "Sign-in failed. Please try again.", Toast.LENGTH_LONG).show()
                    }
                }
            }else{
                Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_LONG).show()
            }
        }
        btnRegisterSender.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, Register())
                .addToBackStack(null)
                .commit()
        }
    }
}