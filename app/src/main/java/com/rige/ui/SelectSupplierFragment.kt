package com.rige.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rige.adapters.SupplierAdapter
import com.rige.databinding.FragmentSelectSupplierBinding
import com.rige.viewmodels.SupplierViewModel

class SelectSupplierFragment : Fragment() {

    private lateinit var binding: FragmentSelectSupplierBinding
    private val viewModel: SupplierViewModel by activityViewModels()
    private lateinit var supplierAdapter: SupplierAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectSupplierBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        viewModel.loadSuppliers()

        binding.cvNoSupplier.setOnClickListener {
            sendSupplierResult(null)
        }
    }

    private fun setupRecyclerView() {
        supplierAdapter = SupplierAdapter { supplier ->
            sendSupplierResult(supplier.id)
        }
        binding.rvSuppliers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = supplierAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.suppliers.observe(viewLifecycleOwner) { list ->
            supplierAdapter.submitList(list)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // puedes mostrar u ocultar un progress bar aquí si lo deseas
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendSupplierResult(supplierId: String?) {
        val action = SelectSupplierFragmentDirections
            .actionSelectSupplierFragmentToGenerateOrderFragment(supplierId)
        findNavController().navigate(action)
    }
}