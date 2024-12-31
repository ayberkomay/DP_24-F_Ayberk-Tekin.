package com.tekin.reciper.ui

import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.tekin.reciper.R
import com.tekin.reciper.model.UserViewModel
import com.tekin.reciper.databinding.FragmentHomeBinding
import com.tekin.reciper.databinding.ItemHomeRecipeBinding
import com.tekin.reciper.ui.recipeui.RecipeDetailFragment

class Home : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserViewModel by activityViewModels()
    private var currentPage = 0
    private val itemsPerPage = 10
    private var isLoading = false
    private var hasMoreItems = true

    private fun loadTopRatedRecipes() {
        if (isLoading || !hasMoreItems) return
        isLoading = true
        binding.loadingProgressBar.visibility = View.VISIBLE

        try {
            viewModel.getTopRatedRecipes(currentPage * itemsPerPage, itemsPerPage) { recipes ->
                if (!isAdded) return@getTopRatedRecipes

                binding.loadingProgressBar.visibility = View.GONE
                if (recipes.isEmpty()) {
                    hasMoreItems = false
                    if (currentPage == 0) {
                        Toast.makeText(context, "No top-rated recipes available.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val container = binding.homeContainer
                    for (r in recipes) {
                        val itemBinding = ItemHomeRecipeBinding.inflate(layoutInflater, container, false)
                        val titleView = itemBinding.homeRecipeTitleTextView
                        val imageView = itemBinding.homeRecipeImageView

                        titleView.text = r.title
                        Glide.with(this)
                            .load(r.imageUrl)
                            .placeholder(R.drawable.baseline_account_circle_24)
                            .centerCrop()
                            .into(imageView)

                        itemBinding.root.setOnClickListener {
                            val fragment = RecipeDetailFragment.newInstance(r.recipeId!!)
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.frame_layout, fragment)
                                .addToBackStack(null)
                                .commit()
                        }
                        container.addView(itemBinding.root)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        binding.homeScrollView.setOnScrollChangeListener { v, _, scrollY, _, oldScrollY ->
            val view = v as ScrollView
            val diff = scrollY - oldScrollY
            if (diff > 0) {
                val child = view.getChildAt(0)
                if (scrollY + view.height >= child.height) {
                    loadTopRatedRecipes()
                }
            }
        }

        loadTopRatedRecipes()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        currentPage = 0
        hasMoreItems = true
        binding.homeContainer.removeAllViews()
        loadTopRatedRecipes()
    }
}