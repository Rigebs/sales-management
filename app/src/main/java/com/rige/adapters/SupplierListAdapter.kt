package com.rige.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rige.databinding.ItemSupplierBinding
import com.rige.models.Supplier

class SupplierListAdapter(
    private val onItemClick: (Supplier) -> Unit
) : ListAdapter<Supplier, SupplierListAdapter.SupplierViewHolder>(SupplierDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupplierViewHolder {
        val binding = ItemSupplierBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SupplierViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SupplierViewHolder, position: Int) {
        val supplier = getItem(position)
        holder.bind(supplier)
    }

    inner class SupplierViewHolder(
        private val binding: ItemSupplierBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(supplier: Supplier) {
            binding.textViewSupplierName.text = supplier.name
            binding.textViewSupplierPhone.text = supplier.phone
            binding.root.setOnClickListener { onItemClick(supplier) }
        }
    }

    class SupplierDiffCallback : DiffUtil.ItemCallback<Supplier>() {
        override fun areItemsTheSame(oldItem: Supplier, newItem: Supplier): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Supplier, newItem: Supplier): Boolean {
            return oldItem == newItem
        }
    }
}
