package com.rige.adapters

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.rige.R
import com.rige.models.SaleCustomer
import org.threeten.bp.format.DateTimeFormatter

class SaleListAdapter(
    private val sales: MutableList<SaleCustomer>,
    private val onClick: (SaleCustomer) -> Unit = {},
    private val onToggleStatus: (SaleCustomer) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
    }

    var showLoader = false

    override fun getItemCount(): Int = sales.size + if (showLoader) 1 else 0

    override fun getItemViewType(position: Int): Int {
        return if (position == sales.size && showLoader) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_ITEM) {
            val view = inflater.inflate(R.layout.item_sale_card, parent, false)
            SaleViewHolder(view, onClick, onToggleStatus)
        } else {
            val view = inflater.inflate(R.layout.item_loading_footer, parent, false)
            LoaderViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SaleViewHolder && position < sales.size) {
            holder.bind(sales[position])
        }
    }

    fun addSales(newSales: List<SaleCustomer>) {
        val start = sales.size
        sales.addAll(newSales)
        notifyItemRangeInserted(start, newSales.size)
    }

    fun showLoadingFooter(show: Boolean) {
        if (show == showLoader) return // No hacer nada si no cambia

        val action = Runnable {
            val hadLoader = showLoader
            showLoader = show
            if (hadLoader && !show) {
                notifyItemRemoved(sales.size)
            } else if (!hadLoader && show) {
                notifyItemInserted(sales.size)
            }
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            Handler(Looper.getMainLooper()).post(action)
        } else {
            action.run()
        }
    }

    class SaleViewHolder(
        itemView: View,
        private val onClick: (SaleCustomer) -> Unit,
        private val onToggleStatus: (SaleCustomer) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvDate = itemView.findViewById<TextView>(R.id.tvSaleDate)
        private val tvName = itemView.findViewById<TextView>(R.id.tvCustomerName)
        private val tvTotal = itemView.findViewById<TextView>(R.id.tvSaleTotal)
        private val btnStatus = itemView.findViewById<ImageButton>(R.id.btnSaleStatus)

        fun bind(sale: SaleCustomer) {
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
            tvDate.text = sale.date.format(formatter)
            tvName.text = "Cliente: ${sale.customerName ?: "Sin nombre"}"
            tvTotal.text = "Total: S/ ${sale.total.setScale(2)}"

            val context = itemView.context
            btnStatus.setImageResource(if (sale.isPaid) R.drawable.ic_check else R.drawable.ic_warn)
            btnStatus.setColorFilter(
                context.getColor(if (sale.isPaid) R.color.green_500 else R.color.red_500)
            )

            itemView.setOnClickListener { onClick(sale) }
            btnStatus.setOnClickListener { onToggleStatus(sale) }
        }
    }

    fun updateSalesWithDiff(newSales: List<SaleCustomer>) {
        val diffCallback = SaleDiffCallback(sales, newSales)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        sales.clear()
        sales.addAll(newSales)
        diffResult.dispatchUpdatesTo(this)
    }

    class LoaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class SaleDiffCallback(
        private val oldList: List<SaleCustomer>,
        private val newList: List<SaleCustomer>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos].id == newList[newPos].id
        }

        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos] == newList[newPos]
        }
    }
}