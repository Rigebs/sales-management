package com.rige.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.rige.adapters.SaleDetailAdapter
import com.rige.databinding.FragmentSaleDetailsBinding
import com.rige.utils.formatToReadable
import com.rige.viewmodels.SaleViewModel

class SaleDetailsFragment : Fragment() {

    private val viewModel: SaleViewModel by activityViewModels()
    private lateinit var binding: FragmentSaleDetailsBinding
    private lateinit var adapter: SaleDetailAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSaleDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val saleId = arguments?.getString("saleId")

        adapter = SaleDetailAdapter()
        binding.recyclerSaleDetails.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSaleDetails.adapter = adapter

        saleId?.let {
            viewModel.getSaleWithDetailsById(it)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.saleWithDetails.observe(viewLifecycleOwner) { saleWithDetails ->
            if (!saleWithDetails.isNullOrEmpty()) {
                val saleHeader = saleWithDetails.first()
                binding.tvSaleDate.text = saleHeader.date.formatToReadable()
                binding.tvTotalAmount.text = "S/. %.2f".format(saleHeader.total)
                binding.tvStatus.text = if (saleHeader.isPaid) "Pagado" else "Pendiente"
                binding.tvCustomerName.text = saleHeader.customerName ?: "Varios"

                adapter.submitList(saleWithDetails)
            }
        }
    }
}