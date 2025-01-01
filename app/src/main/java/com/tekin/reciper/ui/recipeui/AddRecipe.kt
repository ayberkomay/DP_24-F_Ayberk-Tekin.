package com.tekin.reciper.ui.recipeui

import android.app.Activity
import android.content.Intent
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
import com.tekin.reciper.data.RecipeData
import com.tekin.reciper.model.UserViewModel
import com.tekin.reciper.databinding.FragmentRecipeAddBinding

class AddRecipe : Fragment(R.layout.fragment_recipe_add) {
    private lateinit var binding: FragmentRecipeAddBinding
    private val viewModel: UserViewModel by activityViewModels()
    private val PICK_IMAGE_REQUEST = 101
    private var selectedImageUri: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRecipeAddBinding.bind(view)

        val selectImageButton = binding.selectImageButton
        val saveRecipeButton = binding.saveRecipeButton
        val backButton = binding.backButton
        val instructions = binding.instructionsEditText
        val title = binding.titleEditText


        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        saveRecipeButton.setOnClickListener {
            val title = title.text.toString().trim()
            val instructions = instructions.text.toString().trim()

            if (title.isNotEmpty() && instructions.isNotEmpty() && selectedImageUri != null) {
                val userID = viewModel.getCurrentUserID()
                if (userID != null) {
                    val recipeID = viewModel.generateRecipeId()
                    viewModel.uploadRecipeImageUri(userID, recipeID, selectedImageUri!!) { success, imageUrl, errorMsg ->
                        if (success && imageUrl != null) {
                            val recipeData = RecipeData(
                                recipeId = recipeID,
                                userID = userID,
                                title = title,
                                instructions = instructions,
                                imageUrl = imageUrl
                            )
                            viewModel.addRecipe(recipeData) { recipeSuccess, recipeError ->
                                if (recipeSuccess) {
                                    Toast.makeText(context, "Recipe added successfully!", Toast.LENGTH_SHORT).show()
                                    parentFragmentManager.popBackStack()
                                } else {
                                    Toast.makeText(context, "Failed to add recipe: $recipeError", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Image upload failed: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Please select an image, and fill in all fields.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            Glide.with(this)
                .load(selectedImageUri)
                .apply(RequestOptions.centerCropTransform())
                .placeholder(R.drawable.baseline_account_circle_24)
                .into(binding.recipeImageView)
        }
    }
}
