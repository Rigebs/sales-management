package com.rige.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.rige.adapters.OrderDetailAdapter
import com.rige.databinding.FragmentOrderDetailsBinding
import com.rige.utils.formatDecimal
import com.rige.utils.formatToReadable
import com.rige.viewmodels.OrderViewModel

class OrderDetailsFragment : Fragment() {

    private lateinit var binding: FragmentOrderDetailsBinding
    private val viewModel: OrderViewModel by activityViewModels()

    private lateinit var adapter: OrderDetailAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val orderId = arguments?.getString("orderId")

        if (orderId != null) {
            viewModel.loadOrderDetails(orderId)
        } else {
            Toast.makeText(requireContext(), "No se recibió el ID del pedido", Toast.LENGTH_SHORT).show()
            return
        }

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = OrderDetailAdapter()
        binding.recyclerOrderDetails.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerOrderDetails.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.orderDetails.observe(viewLifecycleOwner) { details ->
            if (!details.isNullOrEmpty()) {
                val saleHeader = details.first()
                binding.tvOrderDate.text = saleHeader.date.formatToReadable()
                binding.tvOrderTotal.text = "S/. ${saleHeader.total.formatDecimal(2)}"
                binding.tvSupplierName.text = saleHeader.supplierName ?: "No especificado"

                adapter.submitList(details)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}