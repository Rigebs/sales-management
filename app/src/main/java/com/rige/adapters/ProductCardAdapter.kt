package com.rige.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private val onAddClicked: (Product) -> Unit
) : ListAdapter<Product, ProductCardAdapter.ProductViewHolder>(ProductDiffCallback()) {

    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgProduct: ImageView = view.findViewById(R.id.imgProduct)
        val tvProductName: TextView = view.findViewById(R.id.tvProductName)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val btnAddToCart: ImageButton = view.findViewById(R.id.btnAddToCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_select_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)

        holder.tvProductName.text = product.name
        holder.tvPrice.text = "s/. ${product.sellingPrice}"

        Glide.with(holder.imgProduct.context)
            .load(product.imageUrl)
            .placeholder(R.drawable.ic_broken_image)
            .into(holder.imgProduct)

        holder.btnAddToCart.setOnClickListener {
            onAddClicked(product)
            Toast.makeText(holder.itemView.context, "${product.name} agregado al carrito", Toast.LENGTH_SHORT).show()
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean =
            oldItem == newItem
    }
}