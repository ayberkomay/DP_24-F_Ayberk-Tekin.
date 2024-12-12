package com.tekin.reciper.ui.User

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.tekin.reciper.R
import com.tekin.reciper.UserViewModel
import com.tekin.reciper.databinding.FragmentManagementBinding

class Management : Fragment(R.layout.fragment_management) {
    private lateinit var binding: FragmentManagementBinding
    private val viewModel: UserViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentManagementBinding.bind(view)

        val btnMyInfos = binding.updateInformationsButton
        val btnSignOut = binding.signoutButton
        val btnBack = binding.backButton

        btnMyInfos.setOnClickListener{
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, Informations())
                .addToBackStack(null)
                .commit()
        }

        btnSignOut.setOnClickListener{
            viewModel.signOut()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, UserNotSignedIn())
                .commit()
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
