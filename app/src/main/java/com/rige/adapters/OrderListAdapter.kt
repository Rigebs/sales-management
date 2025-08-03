package com.rige.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rige.R
import com.rige.models.OrderSupplier
import com.rige.utils.formatDecimal
import com.rige.utils.formatToReadable

class OrderListAdapter(
    private val onOrderClick: (OrderSupplier) -> Unit
) : ListAdapter<OrderSupplier, OrderListAdapter.OrderViewHolder>(DiffCallback()) {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val supplierTextView = itemView.findViewById<TextView>(R.id.supplierTextView)
        private val dateTextView = itemView.findViewById<TextView>(R.id.dateTextView)
        private val totalTextView = itemView.findViewById<TextView>(R.id.totalTextView)

        fun bind(order: OrderSupplier) {
            supplierTextView.text = order.supplierName ?: "No especificado"
            dateTextView.text = order.date.formatToReadable()
            totalTextView.text = "S/. ${order.total.formatDecimal(2)}"

            itemView.setOnClickListener { onOrderClick(order) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_list, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<OrderSupplier>() {
        override fun areItemsTheSame(oldItem: OrderSupplier, newItem: OrderSupplier): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: OrderSupplier, newItem: OrderSupplier): Boolean {
            return oldItem == newItem
        }
    }
}
