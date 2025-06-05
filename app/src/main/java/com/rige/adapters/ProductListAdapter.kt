package com.rige.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rige.R
import com.rige.databinding.ItemListProductBinding
import com.rige.models.Product

class ProductListAdapter(
    val onEdit: (Product) -> Unit,
    val onStatusClick: (Product) -> Unit
) : ListAdapter<Product, ProductListAdapter.ProductViewHolder>(DiffCallback()) {

    inner class ProductViewHolder(val binding: ItemListProductBinding) : RecyclerView.ViewHolder(binding.root) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemListProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(old: Product, new: Product) = old.id == new.id
        override fun areContentsTheSame(old: Product, new: Product) = old == new
    }
}