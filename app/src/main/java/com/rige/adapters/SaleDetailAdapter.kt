package com.rige.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rige.R
import com.rige.models.extra.SaleDetailView
import com.rige.utils.formatDecimal

class SaleDetailAdapter : ListAdapter<SaleDetailView, SaleDetailAdapter.ViewHolder>(
    DiffCallback()
) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivProductImage = view.findViewById<ImageView>(R.id.ivProductImage)
        private val tvProductName = view.findViewById<TextView>(R.id.tvProductName)
        private val tvQuantity = view.findViewById<TextView>(R.id.tvQuantity)
        private val tvPrice = view.findViewById<TextView>(R.id.tvPrice)
        private val tvSubtotal = view.findViewById<TextView>(R.id.tvSubtotal)

        fun bind(detail: SaleDetailView) {
            // Nombre
            tvProductName.text = detail.productName

            // Cantidad
            tvQuantity.text = if (detail.isDecimal) {
                // Si es decimal → mostrar 3.200 + unidad si existe
                val qty = detail.quantity.formatDecimal(3) // fuerza 3 decimales
                if (!detail.measureUnit.isNullOrBlank()) {
                    "$qty ${detail.measureUnit}"
                } else {
                    qty
                }
            } else {
                // Si no es decimal → mostrar entero
                detail.quantity
                    .toPlainString()
                    .replace(Regex("\\.0+\$"), "")
            }

            // Precio y subtotal
            tvPrice.text = "s/. ${detail.unitPrice.formatDecimal()}"
            tvSubtotal.text = "s/. ${detail.subtotal.formatDecimal()}"

            // Imagen
            if (!detail.imageUrl.isNullOrBlank()) {
                Glide.with(ivProductImage.context)
                    .load(detail.imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_broken_image)
                    .into(ivProductImage)
            } else {
                ivProductImage.setImageResource(R.drawable.ic_broken_image)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sale_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<SaleDetailView>() {
        override fun areItemsTheSame(old: SaleDetailView, new: SaleDetailView) =
            old.detailId == new.detailId
        override fun areContentsTheSame(old: SaleDetailView, new: SaleDetailView) =
            old == new
    }
}