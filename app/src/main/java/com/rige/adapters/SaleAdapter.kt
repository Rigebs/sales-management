package com.rige.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.rige.R
import com.rige.models.SaleCustomer

class SaleAdapter : RecyclerView.Adapter<SaleAdapter.SaleViewHolder>() {

    private val sales = mutableListOf<SaleCustomer>()

    fun submitSales(newSales: List<SaleCustomer>) {
        val diffCallback = SaleDiffCallback(sales, newSales)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        sales.clear()
        sales.addAll(newSales)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sale_card, parent, false)
        return SaleViewHolder(view)
    }

    override fun getItemCount(): Int = sales.size

    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        holder.bind(sales[position])
    }

    inner class SaleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvSaleDate: TextView = view.findViewById(R.id.tvSaleDate)
        private val tvCustomerName: TextView = view.findViewById(R.id.tvCustomerName)
        private val tvSaleTotal: TextView = view.findViewById(R.id.tvSaleTotal)

        fun bind(sale: SaleCustomer) {
            tvSaleDate.text = sale.date.toString()
            tvCustomerName.text = "Cliente: ${sale.customerName}"
            tvSaleTotal.text = "Total: S/ ${sale.total}"
        }
    }

    private class SaleDiffCallback(
        private val oldList: List<SaleCustomer>,
        private val newList: List<SaleCustomer>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldPos: Int, newPos: Int) =
            oldList[oldPos].id == newList[newPos].id

        override fun areContentsTheSame(oldPos: Int, newPos: Int) =
            oldList[oldPos] == newList[newPos]
    }
}