package com.tekin.reciper.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ScrollView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.tekin.reciper.R
import com.tekin.reciper.model.UserViewModel
import com.tekin.reciper.databinding.FragmentSearchBinding
import com.tekin.reciper.databinding.ItemSearchResultBinding
import com.tekin.reciper.ui.recipeui.RecipeDetailFragment

class Search : Fragment(R.layout.fragment_search) {
    private lateinit var binding: FragmentSearchBinding
    private val viewModel: UserViewModel by activityViewModels()
    private var currentPage = 0
    private val itemsPerPage = 10
    private var isLoading = false
    private var hasMoreItems = true
    private var currentQuery = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)

        binding.searchScrollView.setOnScrollChangeListener { v, _, scrollY, _, oldScrollY ->
            val view = v as ScrollView
            val diff = scrollY - oldScrollY
            if (diff > 0) {
                val child = view.getChildAt(0)
                if (scrollY + view.height >= child.height) {
                    loadMoreResults()
                }
            }
        }

        val searchInput = binding.searchInput
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                performSearch(query)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun performSearch(query: String) {
        currentQuery = query
        currentPage = 0
        hasMoreItems = true
        binding.searchContainer.removeAllViews()

        if (query.isNotEmpty()) {
            loadMoreResults()
        }
    }

    private fun loadMoreResults() {
        if (isLoading || !hasMoreItems || currentQuery.isEmpty()) return
        isLoading = true
        binding.loadingProgressBar.visibility = View.VISIBLE

        try {
            viewModel.searchRecipes(currentQuery, currentPage * itemsPerPage, itemsPerPage) { recipes ->
                if (!isAdded) return@searchRecipes

                binding.loadingProgressBar.visibility = View.GONE
                if (recipes.isEmpty()) {
                    hasMoreItems = false
                } else {
                    val container = binding.searchContainer
                    for (r in recipes) {
                        val itemBinding = ItemSearchResultBinding.inflate(layoutInflater, container, false)
                        val titleView = itemBinding.searchResultTitleTextView
                        val imageView = itemBinding.searchResultImageView

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
            Toast.makeText(context, "Error searching recipes: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
