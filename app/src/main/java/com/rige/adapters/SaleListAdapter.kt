package com.rige.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rige.R
import com.rige.models.SaleCustomer
import org.threeten.bp.format.DateTimeFormatter

class SaleListAdapter(
    private val onClick: (SaleCustomer) -> Unit,
    private val onToggleStatus: (SaleCustomer) -> Unit,
) : ListAdapter<SaleCustomer, SaleListAdapter.SaleViewHolder>(SaleDiffCallback()) {

    inner class SaleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvSaleDate)
        private val tvTotal: TextView = itemView.findViewById(R.id.tvSaleTotal)
        private val btnStatus: ImageButton = itemView.findViewById(R.id.btnSaleStatus)
        private val tvCustomer: TextView = itemView.findViewById(R.id.tvCustomerName)

        fun bind(sale: SaleCustomer) {
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
            tvDate.text = sale.date.format(formatter)
            tvTotal.text = "Total: S/ ${sale.total}"
            tvCustomer.text = "Cliente: ${sale.customerName ?: "Sin cliente"}"

            if (sale.isPaid) {
                btnStatus.setImageResource(R.drawable.ic_check)
                btnStatus.imageTintList = ContextCompat.getColorStateList(itemView.context, R.color.green_500)
            } else {
                btnStatus.setImageResource(R.drawable.ic_warn)
                btnStatus.imageTintList = ContextCompat.getColorStateList(itemView.context, R.color.red_500)
            }

            btnStatus.setOnClickListener {
                if (!sale.isPaid) {
                    onToggleStatus(sale)
                }
            }

            itemView.setOnClickListener { onClick(sale) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sale_card, parent, false)
        return SaleViewHolder(view)
    }

    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SaleDiffCallback : DiffUtil.ItemCallback<SaleCustomer>() {
        override fun areItemsTheSame(oldItem: SaleCustomer, newItem: SaleCustomer): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SaleCustomer, newItem: SaleCustomer): Boolean {
            return oldItem == newItem
        }
    }
}
