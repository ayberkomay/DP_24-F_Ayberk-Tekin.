package com.tekin.reciper.ui.User

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.tekin.reciper.R
import com.tekin.reciper.UserViewModel
import com.tekin.reciper.databinding.FragmentUserSignedinBinding
import java.io.ByteArrayOutputStream

class UserSignedIn : Fragment(R.layout.fragment_user_signedin) {
    private lateinit var binding: FragmentUserSignedinBinding
    private val viewModel: UserViewModel by activityViewModels()
    private val PICK_IMAGE_REQUEST = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserSignedinBinding.bind(view)

        val managementButton = binding.managementButton
        val profileImageView = binding.profileImageView
        val usernameTextView = binding.usernameTextView
        val bioTextView = binding.bioTextView
        val bioEditText = binding.bioEditText
        val editBioButton = binding.editBioButton
        val saveBioButton = binding.saveBioButton
        val addRecipeButton = binding.addRecipeButton

        viewModel.currentUserData.observe(viewLifecycleOwner) { userData ->
            if (userData != null) {
                usernameTextView.text = userData.name ?: "Username"
                bioTextView.text = userData.bio ?: "Your bio here..."
                loadProfileImage(profileImageView)
            } else {
                Toast.makeText(context, "User data not available", Toast.LENGTH_SHORT).show()
            }
        }

        profileImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        editBioButton.setOnClickListener {
            bioTextView.visibility = View.GONE
            bioEditText.visibility = View.VISIBLE
            bioEditText.setText(bioTextView.text.toString())
            saveBioButton.visibility = View.VISIBLE
            editBioButton.visibility = View.GONE
        }

        saveBioButton.setOnClickListener {
            val newBio = bioEditText.text.toString()
            val updatedFields = mapOf("bio" to newBio)
            viewModel.updateUserData(updatedFields, null) { success, errorMessage ->
                if (success) {
                    bioTextView.text = newBio
                    bioTextView.visibility = View.VISIBLE
                    bioEditText.visibility = View.GONE
                    saveBioButton.visibility = View.GONE
                    editBioButton.visibility = View.VISIBLE
                    Toast.makeText(context, "Bio updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Update failed: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
        }

        addRecipeButton.setOnClickListener {/*
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, AddReceipt())
                .addToBackStack(null)
                .commit()*/
        }

        managementButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, Management())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri? = data.data
            if (imageUri != null) {
                Glide.with(this)
                    .load(imageUri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.profileImageView)

                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val dataBytes = baos.toByteArray()

                    viewModel.uploadProfileImage(dataBytes) { success, errorMsg ->
                        if (success) {
                            Toast.makeText(context, "Profile image uploaded", Toast.LENGTH_SHORT).show()
                            loadProfileImage(binding.profileImageView)
                        } else {
                            Toast.makeText(context, "Profile image upload failed: $errorMsg", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to process image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadProfileImage(profileImageView: View) {
        viewModel.getProfileImageUri { uri ->
            if (uri != null) {
                Glide.with(this)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .error(R.drawable.baseline_account_circle_24)
                    .into(binding.profileImageView)
            } else {
                Glide.with(this)
                    .load(R.drawable.baseline_account_circle_24)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.profileImageView)
            }
        }
    }
}
