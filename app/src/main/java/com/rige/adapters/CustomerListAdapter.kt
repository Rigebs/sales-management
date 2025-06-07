package com.rige.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rige.databinding.ItemListCustomerBinding
import com.rige.models.Customer

class CustomerListAdapter(
    val onEdit: (Customer) -> Unit,
    val onInvoiceClick: (Customer) -> Unit
) : ListAdapter<Customer, CustomerListAdapter.CustomerViewHolder>(DiffCallback()) {

    inner class CustomerViewHolder(val binding: ItemListCustomerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(customer: Customer) {
            val fullName = "${customer.name} ${customer.paternalSurname} ${customer.maternalSurname.orEmpty()}"
            binding.tvName.text = fullName
            binding.tvPhone.text = "Teléfono: ${customer.phoneNumber ?: "N.A."}"
            binding.tvAddress.text = "Dirección: ${customer.address ?: "N.A."}"

            binding.root.setOnClickListener { onEdit(customer) }
            binding.btnInvoice.setOnClickListener { onInvoiceClick(customer) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val binding = ItemListCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Customer>() {
        override fun areItemsTheSame(old: Customer, new: Customer): Boolean = old.id == new.id
        override fun areContentsTheSame(old: Customer, new: Customer): Boolean = old == new
    }
}