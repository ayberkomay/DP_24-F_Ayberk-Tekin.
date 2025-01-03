package com.tekin.reciper.ui.User.others

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tekin.reciper.R
import com.tekin.reciper.model.UserViewModel
import com.tekin.reciper.databinding.FragmentUserPublicProfileBinding
import com.tekin.reciper.ui.recipeui.RecipeDetailFragment
import com.tekin.reciper.ui.recipeui.RecipeListAdapter

class UserPublicProfile : Fragment(R.layout.fragment_user_public_profile) {
    private lateinit var binding: FragmentUserPublicProfileBinding
    private val viewModel: UserViewModel by activityViewModels()
    private var userId: String? = null
    private var adapter: RecipeListAdapter? = null
    private var currentPage = 0
    private val itemsPerPage = 10
    private var isLoading = false
    private var hasMoreItems = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserPublicProfileBinding.bind(view)
        userId = arguments?.getString("userId")

        val displayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        val columnCount = (screenWidthDp / 180).toInt().coerceIn(2, 4)

        val recyclerView = binding.publicUserRecipesRecyclerView
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
                        userId?.let { loadPublicRecipes(it) }
                    }
                }
            }
        })

        userId?.let { id ->
            viewModel.getUserDataById(id) { userData ->
                if (userData != null) {
                    val usernameView = binding.publicUsernameTextView
                    val bioView = binding.publicBioTextView
                    val profileImageView = binding.publicProfileImageView

                    usernameView.text = userData.name ?: "User"
                    bioView.text = userData.bio ?: ""

                    viewModel.getProfileImageUriByUserId(id) { uri ->
                        Glide.with(this)
                            .load(uri ?: R.drawable.baseline_account_circle_24)
                            .circleCrop()
                            .placeholder(R.drawable.baseline_account_circle_24)
                            .error(R.drawable.baseline_account_circle_24)
                            .into(profileImageView)
                    }
                } else {
                    Toast.makeText(context, "User data not available", Toast.LENGTH_SHORT).show()
                }
            }
            loadPublicRecipes(id)
        } ?: run {
            Toast.makeText(context, "User data not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPublicRecipes(pubUserId: String) {
        if (isLoading || !hasMoreItems) return
        isLoading = true
        binding.loadingProgressBar.visibility = View.VISIBLE

        try {
            viewModel.getUserRecipesByUserId(pubUserId, currentPage * itemsPerPage, itemsPerPage) { recipes ->
                if (!isAdded) return@getUserRecipesByUserId

                binding.loadingProgressBar.visibility = View.GONE
                if (recipes.isEmpty()) {
                    hasMoreItems = false
                    if (currentPage == 0) {
                        val recyclerView = binding.publicUserRecipesRecyclerView
                        recyclerView.visibility = View.GONE
                        Toast.makeText(context, "No recipes found for this user", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val recyclerView = binding.publicUserRecipesRecyclerView
                    recyclerView.visibility = View.VISIBLE

                    if (currentPage == 0) {
                        val layoutManager = recyclerView.layoutManager as GridLayoutManager
                        val position = layoutManager.findFirstVisibleItemPosition()

                        adapter = RecipeListAdapter(recipes.toMutableList(), { recipe ->
                            val fragment = RecipeDetailFragment.newInstance(recipe.recipeId!!)
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.frame_layout, fragment)
                                .addToBackStack(null)
                                .commit()
                        }, false)
                        recyclerView.adapter = adapter

                        if (position > 0) {
                            recyclerView.scrollToPosition(position)
                        }
                    } else {
                        adapter?.addItems(recipes)
                    }
                    currentPage++
                }
                isLoading = false
            }
        } catch (e: Exception) {
            if (!isAdded) return
            binding.loadingProgressBar.visibility = View.GONE
            isLoading = false
            Toast.makeText(context, "Error loading recipes: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        currentPage = 0
        hasMoreItems = true
        userId?.let { loadPublicRecipes(it) }
    }

    companion object {
        fun newInstance(userId: String): UserPublicProfile {
            val args = Bundle()
            args.putString("userId", userId)
            val fragment = UserPublicProfile()
            fragment.arguments = args
            return fragment
        }
    }
}
