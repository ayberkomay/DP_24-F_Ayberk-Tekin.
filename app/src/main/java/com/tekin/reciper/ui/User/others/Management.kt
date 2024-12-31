package com.tekin.reciper.ui.User.others

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.tekin.reciper.R
import com.tekin.reciper.model.UserViewModel
import com.tekin.reciper.databinding.FragmentManagementBinding
import com.tekin.reciper.ui.User.UserNotSignedIn

class Management : Fragment(R.layout.fragment_management) {
    private lateinit var binding: FragmentManagementBinding
    private val viewModel: UserViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentManagementBinding.bind(view)

        val updateInformationsButton = binding.updateInformationsButton
        val signoutButton = binding.signoutButton
        val backButton = binding.backButton
        val contactButton = binding.contactButton

        updateInformationsButton.setOnClickListener{
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, Informations())
                .addToBackStack(null)
                .commit()
        }
        contactButton.setOnClickListener{
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, Contact())
                .addToBackStack(null)
                .commit()
        }

        signoutButton.setOnClickListener{
            viewModel.signOut()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, UserNotSignedIn())
                .commit()
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}