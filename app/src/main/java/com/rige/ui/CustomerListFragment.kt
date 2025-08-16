package com.rige.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rige.R
import com.rige.adapters.CustomerListAdapter
import com.rige.databinding.FragmentCustomerListBinding
import com.rige.models.Customer
import com.rige.viewmodels.CustomerViewModel

class CustomerListFragment : Fragment() {

    private lateinit var binding: FragmentCustomerListBinding
    private val viewModel: CustomerViewModel by activityViewModels()
    private lateinit var adapter: CustomerListAdapter

    private var allCustomers: List<Customer> = emptyList()
    private var searchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCustomerListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CustomerListAdapter(
            onEdit = { customer ->
                val action = CustomerListFragmentDirections.actionToFormCustomer(customer.id)
                findNavController().navigate(action)
            },
            onInvoiceClick = { customer ->
                val bundle = bundleOf("customerId" to customer.id)
                findNavController().navigate(R.id.actionToSaleList, bundle)
            },
            onPaymentClick = { customer ->
                val bundle = bundleOf("customerId" to customer.id)
                findNavController().navigate(R.id.actionToDebts, bundle)
            }
        )

        binding.rvCustomers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCustomers.adapter = adapter

        if (viewModel.customers.value.isNullOrEmpty()) {
            viewModel.loadCustomers()
        }

        viewModel.customers.observe(viewLifecycleOwner) { customers ->
            allCustomers = customers
            applyFilters()
        }

        binding.searchViewCustomer.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery = newText ?: ""
                applyFilters()
                return true
            }
        })

        binding.fabAddCustomer.setOnClickListener {
            findNavController().navigate(R.id.actionToFormCustomer)
        }
    }

    private fun applyFilters() {
        val result = allCustomers.filter { customer ->
            val fullName = "${customer.name} ${customer.paternalSurname} ${customer.maternalSurname.orEmpty()}"
            fullName.contains(searchQuery, ignoreCase = true)
        }
        adapter.submitList(result)
    }
}