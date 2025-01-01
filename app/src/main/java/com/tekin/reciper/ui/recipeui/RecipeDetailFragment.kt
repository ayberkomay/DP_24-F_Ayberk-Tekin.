package com.tekin.reciper.ui.recipeui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.tekin.reciper.R
import com.tekin.reciper.model.UserViewModel
import com.tekin.reciper.databinding.FragmentRecipeDetailBinding
import com.tekin.reciper.ui.User.others.UserPublicProfile

class RecipeDetailFragment : Fragment(R.layout.fragment_recipe_detail) {
    private val viewModel: UserViewModel by activityViewModels()
    private lateinit var binding: FragmentRecipeDetailBinding
    private var recipeId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRecipeDetailBinding.bind(view)
        recipeId = arguments?.getString("recipeId")

        val recipeImageView = binding.recipeImageView
        val titleTextView = binding.titleTextView
        val instructionsTextView = binding.instructionsTextView
        val likeButton = binding.likeButton
        val authorLayoutBottom = binding.authorLayoutBottom
        val authorImageViewBottom = binding.authorImageViewBottom
        val authorNameTextViewBottom = binding.authorNameTextViewBottom
        val backButton = binding.backButton

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        if (recipeId != null) {
            viewModel.getRecipeById(recipeId!!) { recipe ->
                if (recipe != null) {

                    Glide.with(this)
                        .load(recipe.imageUrl)
                        .placeholder(R.drawable.baseline_account_circle_24)
                        .centerCrop()
                        .into(recipeImageView)

                    titleTextView.text = recipe.title
                    instructionsTextView.text = recipe.instructions
                    val userID = recipe.userID
                    authorLayoutBottom.visibility = View.VISIBLE

                    if (!userID.isNullOrEmpty()) {
                        viewModel.getUserDataById(userID) { userData ->
                            if (userData != null) {
                                authorNameTextViewBottom.text = userData.name ?: "User"
                                viewModel.getProfileImageUriByUserId(userID) { uri ->
                                    if (uri != null) {
                                        Glide.with(this)
                                            .load(uri)
                                            .circleCrop()
                                            .placeholder(R.drawable.baseline_account_circle_24)
                                            .into(authorImageViewBottom)
                                    } else {
                                        Glide.with(this)
                                            .load(R.drawable.baseline_account_circle_24)
                                            .circleCrop()
                                            .into(authorImageViewBottom)
                                    }
                                }
                                authorLayoutBottom.setOnClickListener {
                                    val fragment = UserPublicProfile.newInstance(userID)
                                    parentFragmentManager.beginTransaction()
                                        .replace(R.id.frame_layout, fragment)
                                        .addToBackStack(null)
                                        .commit()
                                }
                            } else {
                                Log.e("RecipeDetail", "User data is null for userID: $userID")
                                authorNameTextViewBottom.text = "User not found"
                                authorLayoutBottom.setOnClickListener(null)
                            }
                        }
                    } else {
                        Log.e("RecipeDetail", "UserID is null or empty")
                        authorLayoutBottom.visibility = View.GONE
                    }

                    likeButton.setOnClickListener {
                        val currentRecipeId = recipeId!!

                        val alert = AlertDialog.Builder(requireContext())
                            .setTitle("Add to List")
                            .setMessage("Do you want to add this recipe to your list? If it is already in your list, it will be removed.")
                            .setPositiveButton("Do it!") { _, _ ->
                                if (!viewModel.signedIn()) {
                                    val signInAlert = AlertDialog.Builder(requireContext())
                                        .setTitle("Not Signed In")
                                        .setMessage("To like and add recipes to your list, sign in?")
                                        .setPositiveButton("Sign In") { _, _ ->
                                            parentFragmentManager.beginTransaction()
                                                .replace(R.id.frame_layout, com.tekin.reciper.ui.User.UserNotSignedIn())
                                                .commit()
                                        }
                                        .setNegativeButton("Back") { d, _ -> d.dismiss() }
                                        .create()
                                    signInAlert.show()
                                } else {
                                    viewModel.toggleLikeRecipe(currentRecipeId) { liked ->
                                        val alertMessage = if (liked) "Recipe added to your list!" else "Recipe removed from your list!"
                                        val confirmationAlert = AlertDialog.Builder(requireContext())
                                            .setMessage(alertMessage)
                                            .setPositiveButton("OK") { d, _ -> d.dismiss() }
                                            .create()
                                        confirmationAlert.show()
                                    }
                                }
                            }
                            .setNegativeButton("Back") { dialog, _ -> dialog.dismiss() }
                            .create()
                        alert.show()
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(recipeId: String): RecipeDetailFragment {
            val args = Bundle()
            args.putString("recipeId", recipeId)
            val f = RecipeDetailFragment()
            f.arguments = args
            return f
        }
    }
}
