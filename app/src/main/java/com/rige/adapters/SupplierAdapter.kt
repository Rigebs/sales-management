package com.rige.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.rige.R
import com.rige.models.Supplier

class SupplierAdapter(
    private val onClick: (Supplier) -> Unit
) : ListAdapter<Supplier, SupplierAdapter.SupplierViewHolder>(DIFF) {

    inner class SupplierViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvSupplierName)
        private val card: MaterialCardView = itemView as MaterialCardView

        fun bind(supplier: Supplier) {
            tvName.text = supplier.name
            card.setOnClickListener { onClick(supplier) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupplierViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_supplier_card, parent, false)
        return SupplierViewHolder(view)
    }

    override fun onBindViewHolder(holder: SupplierViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Supplier>() {
            override fun areItemsTheSame(old: Supplier, new: Supplier) = old.id == new.id
            override fun areContentsTheSame(old: Supplier, new: Supplier) = old == new
        }
    }
}