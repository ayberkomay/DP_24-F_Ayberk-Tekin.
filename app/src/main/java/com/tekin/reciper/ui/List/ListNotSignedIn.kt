package com.tekin.reciper.ui.List

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.tekin.reciper.R
import com.tekin.reciper.databinding.FragmentListNotSignedinBinding
import com.tekin.reciper.ui.User.UserNotSignedIn

class ListNotSignedIn: Fragment(R.layout.fragment_list_not_signedin){
    private lateinit var binding: FragmentListNotSignedinBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentListNotSignedinBinding.bind(view)

        val btnlistSignIn = binding.buttonlistSignIn

        btnlistSignIn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, UserNotSignedIn())
                .commit()
        }
    }
}