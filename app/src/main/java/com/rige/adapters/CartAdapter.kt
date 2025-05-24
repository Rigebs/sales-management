package com.rige.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rige.R
import com.rige.models.CartItem
import java.math.BigDecimal

class CartAdapter(
    private val onQuantityChange: (CartItem, Int) -> Unit,
    private val onDelete: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imgProduct: ImageView = view.findViewById(R.id.imgProduct)
        private val txtName: TextView = view.findViewById(R.id.txtName)
        private val txtPrice: TextView = view.findViewById(R.id.txtPrice)
        private val txtSubtotal: TextView = view.findViewById(R.id.txtSubtotal)
        private val txtCount: TextView = view.findViewById(R.id.txtQuantity)
        private val btnIncrease: ImageButton = view.findViewById(R.id.btnIncrease)
        private val btnDecrease: ImageButton = view.findViewById(R.id.btnDecrease)
        private val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)

        fun bind(item: CartItem) {
            txtName.text = item.name
            txtPrice.text = "Precio: s/. ${"%.2f".format(item.price)}"
            val subtotal = item.price.multiply(BigDecimal(item.count))
            txtSubtotal.text = "Subtotal: s/. ${"%.2f".format(subtotal)}"
            txtCount.text = item.count.toString()

            Glide.with(itemView.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.ic_broken_image)
                .into(imgProduct)

            btnIncrease.setOnClickListener {
                onQuantityChange(item, item.count + 1)
            }

            btnDecrease.setOnClickListener {
                val newCount = item.count - 1
                if (newCount > 0) {
                    onQuantityChange(item, newCount)
                } else {
                    onDelete(item)
                }
            }

            btnDelete.setOnClickListener {
                onDelete(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}