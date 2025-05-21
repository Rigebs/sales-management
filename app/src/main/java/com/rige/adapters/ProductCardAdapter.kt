package com.rige.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rige.models.Product
import com.rige.R

class ProductCardAdapter(
    private val products: List<Product>,
    private val onAddClicked: (Product) -> Unit
) : RecyclerView.Adapter<ProductCardAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgProduct: ImageView = view.findViewById(R.id.imgProduct)
        val tvProductName: TextView = view.findViewById(R.id.tvProductName)
        val tvBarCode: TextView = view.findViewById(R.id.tvBarCode)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val btnAddToCart: Button = view.findViewById(R.id.btnAddToCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_select_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount() = products.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.tvProductName.text = product.name
        holder.tvBarCode.text = "Código: ${product.barCode}"
        holder.tvPrice.text = "s/. ${product.sellingPrice}"

        // Cargar imagen con Glide o Picasso (o tu librería favorita)
        Glide.with(holder.imgProduct.context)
            .load(product.imageUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(holder.imgProduct)

        holder.btnAddToCart.setOnClickListener {
            onAddClicked(product)
        }
    }
}