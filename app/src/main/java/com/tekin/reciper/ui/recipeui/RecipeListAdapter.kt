package com.tekin.reciper.ui.recipeui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.tekin.reciper.R
import com.tekin.reciper.data.RecipeData
import com.tekin.reciper.databinding.ItemRecipeBinding
import com.tekin.reciper.databinding.ItemEditRecipeBinding

class RecipeListAdapter(
    private val recipes: MutableList<RecipeData>,
    private val onRecipeClicked: (RecipeData) -> Unit,
    private val isEditMode: Boolean = false
) : RecyclerView.Adapter<RecipeListAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(recipe: RecipeData) {
            when (binding) {
                is ItemRecipeBinding -> bindRegularRecipe(binding, recipe)
                is ItemEditRecipeBinding -> bindEditableRecipe(binding, recipe)
            }
        }

        private fun bindRegularRecipe(binding: ItemRecipeBinding, recipe: RecipeData) {
            with(binding) {
                recipeTitleTextView.text = recipe.title
                Glide.with(root.context)
                    .load(recipe.imageUrl)
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .centerCrop()
                    .into(recipeImageView)

                recipeContainer.setOnClickListener {
                    onRecipeClicked(recipe)
                }
            }
        }

        private fun bindEditableRecipe(binding: ItemEditRecipeBinding, recipe: RecipeData) {
            with(binding) {
                recipeTitleTextView.text = recipe.title
                Glide.with(root.context)
                    .load(recipe.imageUrl)
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .centerCrop()
                    .into(recipeImageView)

                editButton.setOnClickListener {
                    onRecipeClicked(recipe)
                }

                recipeContainer.setOnClickListener {
                    onRecipeClicked(recipe)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = if (isEditMode) {
            ItemEditRecipeBinding.inflate(inflater, parent, false)
        } else {
            ItemRecipeBinding.inflate(inflater, parent, false)
        }
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size

    fun updateRecipes(newRecipes: List<RecipeData>) {
        recipes.clear()
        recipes.addAll(newRecipes)
        notifyDataSetChanged()
    }

    fun addItems(newRecipes: List<RecipeData>) {
        val startPosition = recipes.size
        recipes.addAll(newRecipes)
        notifyItemRangeInserted(startPosition, newRecipes.size)
    }
}
