package com.rige.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rige.R
import com.rige.databinding.ItemOrderDetailBinding
import com.rige.models.extra.OrderDetailView

class OrderDetailAdapter : ListAdapter<OrderDetailView, OrderDetailAdapter.ViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<OrderDetailView>() {
        override fun areItemsTheSame(oldItem: OrderDetailView, newItem: OrderDetailView): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: OrderDetailView, newItem: OrderDetailView): Boolean {
            return oldItem.detailId == newItem.detailId
        }
    }

    inner class ViewHolder(private val binding: ItemOrderDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OrderDetailView) {
            binding.tvProductName.text = item.productName
            binding.tvQuantity.text = item.quantity.toString()
            binding.tvPrice.text = "S/. %.2f".format(item.unitPrice)
            binding.tvSubtotal.text = "S/. %.2f".format(item.subtotal)

            val imageUrl = item.imageUrl
            if (!imageUrl.isNullOrBlank()) {
                Glide.with(binding.root.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_broken_image)
                    .error(R.drawable.ic_broken_image)
                    .centerCrop()
                    .into(binding.imgProduct)
            } else {
                binding.imgProduct.setImageResource(R.drawable.ic_broken_image)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}