package com.tekin.reciper.ui.User

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.tekin.reciper.R
import com.tekin.reciper.data.RecipeData
import com.tekin.reciper.model.UserViewModel
import com.tekin.reciper.databinding.FragmentUserSignedinBinding
import com.tekin.reciper.ui.recipeui.EditRecipeFragment
import com.tekin.reciper.ui.recipeui.RecipeListAdapter
import com.tekin.reciper.ui.User.others.Management
import com.tekin.reciper.ui.recipeui.AddRecipe

class UserSignedIn : Fragment(R.layout.fragment_user_signedin) {
    private lateinit var binding: FragmentUserSignedinBinding
    private val viewModel: UserViewModel by activityViewModels()
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var adapter: RecipeListAdapter
    private var currentPage = 0
    private val itemsPerPage = 10
    private var isLoading = false
    private var hasMoreItems = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserSignedinBinding.bind(view)

        val displayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        val columnCount = (screenWidthDp / 180).toInt().coerceIn(2, 4)

        val recyclerView = binding.recipesRecyclerView
        val loadingProgressBar = binding.loadingProgressBar
        val noRecipeTextView = binding.noRecipeTextView
        val managementButton = binding.managementButton
        val profileImageView = binding.profileImageView
        val bioTextView = binding.bioTextView
        val bioEditText = binding.bioEditText
        val editBioButton = binding.editBioButton
        val saveBioButton = binding.saveBioButton
        val addRecipeButton = binding.addRecipeButton

        recyclerView.layoutManager = GridLayoutManager(requireContext(), columnCount)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && hasMoreItems) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                    ) {
                        loadUserDataAndRecipes()
                    }
                }
            }
        })

        viewModel.currentUserData.observe(viewLifecycleOwner) { userData ->
            if (userData != null) {
                val usernameView = binding.usernameTextView
                val bioView = binding.bioTextView

                usernameView.text = userData.name ?: "Username"
                bioView.text = userData.bio ?: ""
                loadProfileImage()
            } else {
                Toast.makeText(context, "User data not available", Toast.LENGTH_SHORT).show()
            }
        }

        bioEditText.filters = arrayOf(InputFilter.LengthFilter(120))

        profileImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
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

        addRecipeButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, AddRecipe())
                .addToBackStack(null)
                .commit()
        }

        managementButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, Management())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        currentPage = 0
        hasMoreItems = true
        loadUserDataAndRecipes()
    }

    private fun loadUserDataAndRecipes() {
        if (isLoading || !hasMoreItems) return
        isLoading = true

        val recyclerView = binding.recipesRecyclerView
        val loadingProgressBar = binding.loadingProgressBar
        val noRecipeTextView = binding.noRecipeTextView

        loadingProgressBar.visibility = View.VISIBLE

        try {
            viewModel.getUserRecipes(currentPage * itemsPerPage, itemsPerPage) { recipes ->
                if (!isAdded) return@getUserRecipes

                loadingProgressBar.visibility = View.GONE
                if (recipes.isEmpty()) {
                    hasMoreItems = false
                    if (currentPage == 0) {
                        noRecipeTextView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }
                } else {
                    noRecipeTextView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                    if (currentPage == 0) {
                        val layoutManager = recyclerView.layoutManager as GridLayoutManager
                        val position = layoutManager.findFirstVisibleItemPosition()

                        adapter = RecipeListAdapter(recipes.toMutableList(), { recipe ->
                            showRecipeOptionsDialog(recipe)
                        }, true)
                        recyclerView.adapter = adapter

                        if (position > 0) {
                            recyclerView.scrollToPosition(position)
                        }
                    } else {
                        adapter.addItems(recipes)
                    }
                    currentPage++
                }
                isLoading = false
            }
        } catch (e: Exception) {
            if (!isAdded) return
            loadingProgressBar.visibility = View.GONE
            isLoading = false
            Toast.makeText(context, "Error loading recipes: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRecipeOptionsDialog(recipe: RecipeData) {
        val options = arrayOf("Edit the recipe", "Delete the recipe")
        AlertDialog.Builder(requireContext())
            .setTitle("Recipe Options")
            .setItems(options) { _, which ->
                if (which == 0) {
                    val editFragment = EditRecipeFragment.newInstance(recipe.recipeId!!)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, editFragment)
                        .addToBackStack(null)
                        .commit()
                } else {
                    val confirmDialog = AlertDialog.Builder(requireContext())
                        .setTitle("Are you sure you want to delete this recipe?")
                        .setPositiveButton("Yes") { _, _ ->
                            val userID = viewModel.getCurrentUserID()
                            if (userID != null) {
                                viewModel.deleteRecipe(recipe.recipeId!!) { success, errorMsg ->
                                    if (success) {
                                        Toast.makeText(context, "Recipe deleted", Toast.LENGTH_SHORT).show()
                                        currentPage = 0
                                        hasMoreItems = true
                                        val recyclerView = binding.recipesRecyclerView
                                        adapter = RecipeListAdapter(mutableListOf(), { recipe ->
                                            showRecipeOptionsDialog(recipe)
                                        }, true)
                                        recyclerView.adapter = adapter
                                        loadUserDataAndRecipes()
                                    } else {
                                        Toast.makeText(context, "Failed to delete: $errorMsg", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                        .setNegativeButton("No") { d, _ -> d.dismiss() }
                        .create()
                    confirmDialog.show()
                }
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == android.app.Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri? = data.data
            if (imageUri != null) {
                Glide.with(this)
                    .load(imageUri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.profileImageView)

                viewModel.uploadProfileImageUri(imageUri) { success, errorMsg ->
                    if (success) {
                        Toast.makeText(context, "Profile image uploaded", Toast.LENGTH_SHORT).show()
                        loadProfileImage()
                    } else {
                        Toast.makeText(context, "Profile image upload failed: $errorMsg", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loadProfileImage() {
        val profileImageView = binding.profileImageView

        viewModel.getProfileImageUri { uri ->
            if (uri != null) {
                Glide.with(this)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .error(R.drawable.baseline_account_circle_24)
                    .into(profileImageView)
            } else {
                Glide.with(this)
                    .load(R.drawable.baseline_account_circle_24)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profileImageView)
            }
        }
    }
}
