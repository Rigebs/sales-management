package com.rige.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rige.R
import com.rige.models.extra.SaleDetailView
import com.rige.utils.formatDecimal

class SaleDetailAdapter : ListAdapter<SaleDetailView, SaleDetailAdapter.ViewHolder>(
    DiffCallback()
) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvProductName = view.findViewById<TextView>(R.id.tvProductName)
        private val tvQuantity = view.findViewById<TextView>(R.id.tvQuantity)
        private val tvPrice = view.findViewById<TextView>(R.id.tvPrice)
        private val tvSubtotal = view.findViewById<TextView>(R.id.tvSubtotal)

        fun bind(detail: SaleDetailView) {
            tvProductName.text = detail.productName
            tvQuantity.text = detail.quantity.toString()
            tvPrice.text = "s/. ${detail.unitPrice.formatDecimal()}"
            tvSubtotal.text = "s/. ${detail.subtotal.formatDecimal()}"
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