package com.rige.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rige.R
import com.rige.models.extra.PurchaseItem
import java.math.RoundingMode

class PurchaseAdapter(
    private val onEdit: (PurchaseItem) -> Unit,
    private val onDelete: (PurchaseItem) -> Unit
) : RecyclerView.Adapter<PurchaseAdapter.PurchaseItemViewHolder>() {

    private val items = mutableListOf<PurchaseItem>()

    fun submitList(newItems: List<PurchaseItem>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = newItems.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition].productId == newItems[newItemPosition].productId
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition] == newItems[newItemPosition]
            }
        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items.clear()
        items.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_purchase, parent, false)
        return PurchaseItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: PurchaseItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class PurchaseItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: PurchaseItem) {
            itemView.findViewById<TextView>(R.id.txtName).text = item.name
            itemView.findViewById<TextView>(R.id.txtPrice).text = "Precio: s/. ${item.unitPrice}"
            itemView.findViewById<TextView>(R.id.txtSubtotal).text = "Subtotal: s/. ${item.subtotal}"

            val quantityText = if (item.measureUnit != null) {
                "${item.quantity.setScale(3, RoundingMode.HALF_UP)} ${item.measureUnit}"
            } else {
                item.quantity.setScale(0, RoundingMode.HALF_UP).toPlainString()
            }

            itemView.findViewById<TextView>(R.id.txtQuantity).text = quantityText

            val imgProduct = itemView.findViewById<ImageView>(R.id.imgProduct)

            if (item.imageUrl.isNotBlank()) {
                Glide.with(itemView)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_broken_image)
                    .into(imgProduct)
            } else {
                imgProduct.setImageResource(R.drawable.ic_broken_image)
            }

            itemView.findViewById<ImageButton>(R.id.btnEdit).setOnClickListener { onEdit(item) }

            itemView.findViewById<ImageButton>(R.id.btnDelete).setOnClickListener { onDelete(item) }
        }
    }
}
