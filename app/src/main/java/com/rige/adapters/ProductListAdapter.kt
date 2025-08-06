package com.rige.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rige.R
import com.rige.databinding.ItemListProductBinding
import com.rige.models.Product

class ProductListAdapter(
    val onEdit: (Product) -> Unit,
    val onStatusClick: (Product) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_LOADING = 1
    }

    private val products = mutableListOf<Product>()
    private var showLoadingFooter = false

    fun submitList(newProducts: List<Product>) {
        val diffCallback = ProductDiffCallback(products, newProducts)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        products.clear()
        products.addAll(newProducts)
        diffResult.dispatchUpdatesTo(this)
    }

    fun showLoading(show: Boolean) {
        if (show == showLoadingFooter) return
        showLoadingFooter = show

        if (show) {
            notifyItemInserted(products.size)
        } else {
            notifyItemRemoved(products.size)
        }
    }

    override fun getItemCount(): Int = products.size + if (showLoadingFooter) 1 else 0

    override fun getItemViewType(position: Int): Int {
        return if (position < products.size) TYPE_ITEM else TYPE_LOADING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_ITEM -> {
                val binding = ItemListProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ProductViewHolder(binding)
            }
            TYPE_LOADING -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loading_footer, parent, false)
                LoadingViewHolder(view)
            }
            else -> throw IllegalArgumentException("Tipo de vista no soportado")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ProductViewHolder && position < products.size) {
            holder.bind(products[position])
        }
    }

    inner class ProductViewHolder(private val binding: ItemListProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvName.text = product.name
            binding.tvPrice.text = "S/. ${product.sellingPrice}"
            binding.tvStock.text = "Stock: ${product.quantity}"

            val statusIconRes = if (product.status) {
                R.drawable.ic_green_circle
            } else {
                R.drawable.ic_red_circle
            }
            binding.centerIcon.setImageResource(statusIconRes)

            if (!product.imageUrl.isNullOrBlank()) {
                Glide.with(binding.productImage.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_broken_image)
                    .into(binding.productImage)
            } else {
                binding.productImage.setImageResource(R.drawable.ic_broken_image)
            }

            binding.centerIcon.setOnClickListener { onStatusClick(product) }
            binding.root.setOnClickListener { onEdit(product) }
        }
    }

    class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class ProductDiffCallback(
        private val oldList: List<Product>,
        private val newList: List<Product>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean =
            oldList[oldPos].id == newList[newPos].id

        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean =
            oldList[oldPos] == newList[newPos]
    }
}