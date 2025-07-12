package com.rige.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.rige.R
import com.rige.models.SaleCustomer
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

class SaleListAdapter(
    private val onItemClick: (saleId: String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val sales = mutableListOf<SaleCustomer>()
    private var showLoadingFooter = false

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_LOADING = 1
    }

    fun submitSales(newSales: List<SaleCustomer>) {

        val diffCallback = SaleDiffCallback(sales, newSales)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        sales.clear()
        sales.addAll(newSales)
        diffResult.dispatchUpdatesTo(this)
    }


    fun showLoading(show: Boolean) {
        if (show == showLoadingFooter) return
        showLoadingFooter = show

        if (show) {
            notifyItemInserted(sales.size)
        } else {
            notifyItemRemoved(sales.size)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < sales.size) TYPE_ITEM else TYPE_LOADING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_sale, parent, false)
            SaleViewHolder(view, onItemClick)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_loading_footer, parent, false)
            LoadingViewHolder(view)
        }
    }

    override fun getItemCount(): Int = sales.size + if (showLoadingFooter) 1 else 0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SaleViewHolder && position < sales.size) {
            holder.bind(sales[position])
        }
    }

    inner class SaleViewHolder(
        view: View,
        private val onItemClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        private val tvSaleDate: TextView = view.findViewById(R.id.tvSaleDate)
        private val tvCustomerName: TextView = view.findViewById(R.id.tvCustomerName)
        private val tvSaleTotal: TextView = view.findViewById(R.id.tvSaleTotal)

        fun bind(sale: SaleCustomer) {
            val format = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a", Locale.getDefault())
            tvSaleDate.text = sale.date.format(format)
            tvCustomerName.text = "Cliente: ${sale.customerName ?: "Varios"}"
            tvSaleTotal.text = "Total: S/ ${sale.total}"

            val iconRes = if (sale.isPaid) R.drawable.ic_check else R.drawable.ic_warn
            val tintRes = if (sale.isPaid) R.color.green_500 else R.color.red_500

            itemView.findViewById<ImageButton>(R.id.btnSaleStatus).apply {
                setImageResource(iconRes)
                setColorFilter(ContextCompat.getColor(context, tintRes))
            }

            itemView.setOnClickListener {
                onItemClick(sale.id)
            }
        }
    }

    class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view)

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