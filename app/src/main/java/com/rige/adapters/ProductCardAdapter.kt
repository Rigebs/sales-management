package com.rige.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rige.models.Product
import com.rige.R

class ProductCardAdapter(
    private val onAddClicked: (Product) -> Unit,
    private val onDeepSearchClick: () -> Unit
) : ListAdapter<Product, RecyclerView.ViewHolder>(ProductDiffCallback()) {

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_LOADING = 1
        private const val TYPE_DEEP_SEARCH = 2
    }

    private var showLoadingFooter = false
    private var showDeepSearchButton = false

    fun showLoading(show: Boolean) {
        if (show == showLoadingFooter) return
        showLoadingFooter = show
        notifyDataSetChanged()
    }

    fun setShowDeepSearchButton(show: Boolean) {
        if (show == showDeepSearchButton) return
        showDeepSearchButton = show
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        var count = super.getItemCount()
        if (showLoadingFooter) count += 1
        if (showDeepSearchButton) count += 1
        return count
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position < super.getItemCount() -> TYPE_ITEM
            showLoadingFooter && position == super.getItemCount() -> TYPE_LOADING
            else -> TYPE_DEEP_SEARCH
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_ITEM -> {
                val view = inflater.inflate(R.layout.item_select_product, parent, false)
                ProductViewHolder(view)
            }
            TYPE_LOADING -> {
                val view = inflater.inflate(R.layout.item_loading_footer, parent, false)
                LoadingViewHolder(view)
            }
            TYPE_DEEP_SEARCH -> {
                val view = inflater.inflate(R.layout.item_deep_search_footer, parent, false)
                DeepSearchViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProductViewHolder -> {
                val product = getItem(position)
                with(holder) {
                    tvProductName.text = product.name
                    tvPrice.text = "s/. ${product.sellingPrice}"

                    if (!product.imageUrl.isNullOrBlank()) {
                        Glide.with(imgProduct.context)
                            .load(product.imageUrl)
                            .placeholder(R.drawable.ic_broken_image)
                            .error(R.drawable.ic_broken_image)
                            .into(imgProduct)
                    } else {
                        imgProduct.setImageResource(R.drawable.ic_broken_image)
                    }

                    btnAddToCart.setOnClickListener {
                        onAddClicked(product)
                        Toast.makeText(
                            itemView.context,
                            "${product.name} agregado al carrito",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            is DeepSearchViewHolder -> {
                holder.btnDeepSearch.setOnClickListener {
                    onDeepSearchClick()
                }
            }

            // No need to bind loading holder
        }
    }

    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgProduct: ImageView = view.findViewById(R.id.imgProduct)
        val tvProductName: TextView = view.findViewById(R.id.tvProductName)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val btnAddToCart: ImageButton = view.findViewById(R.id.btnAddToCart)
    }

    inner class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view)

    inner class DeepSearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val btnDeepSearch: Button = view.findViewById(R.id.btnDeepSearch)
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean =
            oldItem == newItem
    }
}