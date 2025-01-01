package com.tekin.reciper.ui.List

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tekin.reciper.R
import com.tekin.reciper.model.UserViewModel
import com.tekin.reciper.databinding.FragmentListSignedinBinding
import com.tekin.reciper.ui.recipeui.RecipeDetailFragment
import com.tekin.reciper.ui.recipeui.RecipeListAdapter
import android.widget.Toast

class ListSignedIn : Fragment(R.layout.fragment_list_signedin) {
    private var _binding: FragmentListSignedinBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserViewModel by activityViewModels()
    private var currentPage = 0
    private val itemsPerPage = 10
    private var isLoading = false
    private var hasMoreItems = true
    private var adapter: RecipeListAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentListSignedinBinding.bind(view)

        val displayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        val columnCount = (screenWidthDp / 180).toInt().coerceIn(2, 4)

        val recyclerView = binding.listSignedInRecyclerView
        val loadingProgressBar = binding.loadingProgressBar
        val textView = binding.textView

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
                        loadFavorites()
                    }
                }
            }
        })

        loadFavorites()
    }

    private fun loadFavorites() {
        if (isLoading || !hasMoreItems) return
        isLoading = true

        val recyclerView = binding.listSignedInRecyclerView
        val loadingProgressBar = binding.loadingProgressBar
        val textView = binding.textView

        loadingProgressBar.visibility = View.VISIBLE

        try {
            viewModel.getFavorites(currentPage * itemsPerPage, itemsPerPage) { favorites ->
                if (!isAdded) return@getFavorites

                loadingProgressBar.visibility = View.GONE
                if (favorites.isEmpty()) {
                    hasMoreItems = false
                    if (currentPage == 0) {
                        textView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }
                } else {
                    textView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                    if (currentPage == 0) {
                        val layoutManager = recyclerView.layoutManager as GridLayoutManager
                        val position = layoutManager.findFirstVisibleItemPosition()

                        adapter = RecipeListAdapter(favorites.toMutableList(), { recipe ->
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
                        adapter?.addItems(favorites)
                    }
                    currentPage++
                }
                isLoading = false
            }
        } catch (e: Exception) {
            if (!isAdded) return
            loadingProgressBar.visibility = View.GONE
            isLoading = false
            Toast.makeText(context, "Error loading favorites: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        val recyclerView = binding.listSignedInRecyclerView
        val layoutManager = recyclerView.layoutManager as GridLayoutManager
        val position = layoutManager.findFirstVisibleItemPosition()

        currentPage = 0
        hasMoreItems = true
        loadFavorites()

        if (position > 0) {
            recyclerView.scrollToPosition(position)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
