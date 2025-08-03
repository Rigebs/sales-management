package com.rige.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rige.R
import com.rige.models.Product

class SelectProductPurchaseAdapter(
    private val onAddToPurchaseClick: (Product) -> Unit,
    private val onDeepSearchClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_PRODUCT = 0
        private const val VIEW_TYPE_LOADING = 1
        private const val VIEW_TYPE_DEEP_SEARCH = 2
    }

    private var productList: List<Product> = emptyList()
    private var isLoadingFooterVisible: Boolean = false
    private var showDeepSearchButton: Boolean = false

    fun updateList(newList: List<Product>, showFooter: Boolean = false) {
        val oldCombined = getCombinedList(productList, isLoadingFooterVisible, showDeepSearchButton)
        val newCombined = getCombinedList(newList, showFooter, showDeepSearchButton)

        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = oldCombined.size
            override fun getNewListSize() = newCombined.size

            override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
                val oldItem = oldCombined[oldPos]
                val newItem = newCombined[newPos]

                return when {
                    oldItem is Product && newItem is Product -> oldItem.id == newItem.id
                    oldItem == null && newItem == null -> true
                    else -> false
                }
            }

            override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
                val oldItem = oldCombined[oldPos]
                val newItem = newCombined[newPos]
                return oldItem == newItem
            }
        })

        productList = newList
        isLoadingFooterVisible = showFooter
        diffResult.dispatchUpdatesTo(this)
    }

    private fun getCombinedList(
        products: List<Product>,
        showFooter: Boolean,
        showDeepSearch: Boolean
    ): List<Any?> {
        var list: List<Any?> = products
        if (showDeepSearch) list += "DEEP_SEARCH"
        if (showFooter) list += null
        return list
    }

    fun updateDeepSearchButton(show: Boolean) {
        if (show == showDeepSearchButton) return
        val currentList = productList
        val currentFooter = isLoadingFooterVisible

        showDeepSearchButton = show
        updateList(currentList, currentFooter)
    }

    override fun getItemCount(): Int {
        return productList.size +
                (if (showDeepSearchButton) 1 else 0) +
                (if (isLoadingFooterVisible) 1 else 0)
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position < productList.size -> VIEW_TYPE_PRODUCT
            showDeepSearchButton && position == productList.size -> VIEW_TYPE_DEEP_SEARCH
            else -> VIEW_TYPE_LOADING
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_PRODUCT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_select_product_purchase, parent, false)
                ProductViewHolder(view)
            }
            VIEW_TYPE_LOADING -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading_footer, parent, false)
                LoadingViewHolder(view)
            }
            VIEW_TYPE_DEEP_SEARCH -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_deep_search_footer, parent, false)
                DeepSearchViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ProductViewHolder && position < productList.size) {
            holder.bind(productList[position])
        }
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvPurchasePrice: TextView = itemView.findViewById(R.id.tvPurchasePrice)
        private val tvStock: TextView = itemView.findViewById(R.id.tvStock)
        private val btnAddToPurchase: ImageButton = itemView.findViewById(R.id.btnAddToPurchase)

        fun bind(product: Product) {
            tvProductName.text = product.name
            tvPurchasePrice.text = "Costo: S/. %.2f".format(product.costPrice)

            val stockText = if (product.isDecimal) {
                val unit = product.measureUnit ?: ""
                "Stock: %.3f $unit".format(product.quantity)
            } else {
                "Stock: ${product.quantity.toBigInteger()} unidades"
            }

            tvStock.text = stockText

            if (!product.imageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.ic_broken_image)
                    .into(imgProduct)
            } else {
                imgProduct.setImageResource(R.drawable.ic_broken_image)
            }

            val centerIcon: ImageView = itemView.findViewById(R.id.centerIcon)
            centerIcon.setImageResource(
                if (product.status) R.drawable.ic_green_circle
                else R.drawable.ic_red_circle
            )

            btnAddToPurchase.setOnClickListener { onAddToPurchaseClick(product) }
        }
    }

    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class DeepSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val btnDeepSearch: Button = itemView.findViewById(R.id.btnDeepSearch)

        init {
            btnDeepSearch.setOnClickListener { onDeepSearchClick() }
        }
    }
}