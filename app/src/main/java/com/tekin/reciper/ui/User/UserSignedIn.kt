package com.tekin.reciper.ui.User

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.tekin.reciper.R
import com.tekin.reciper.UserViewModel
import com.tekin.reciper.databinding.FragmentUserSignedinBinding

/* I'll add,
*  update informations settings,
*  personal informations,
*  profile photo
*  my receipts area?*/

class UserSignedIn : Fragment(R.layout.fragment_user_signedin) {
    private lateinit var binding: FragmentUserSignedinBinding
    private val viewModel: UserViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserSignedinBinding.bind(view)

        val mailtext = binding.textUserEmail
        val btnSignOut = binding.buttonSignout

        val userData = viewModel.currentUserData.value
        userData?.let {
            mailtext.text = it.email
        }

        btnSignOut.setOnClickListener{
            viewModel.signOut()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, UserNotSignedIn())
                .addToBackStack(null)
                .commit()

        }
    }


}