package com.rige.ui

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rige.R
import com.rige.adapters.SaleListAdapter
import com.rige.databinding.FragmentSaleListBinding
import com.rige.viewmodels.SaleViewModel
import androidx.core.widget.doAfterTextChanged

class SaleListFragment : Fragment() {

    private lateinit var binding: FragmentSaleListBinding
    private val viewModel: SaleViewModel by activityViewModels()
    private lateinit var adapter: SaleListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSaleListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvSales)
        val etSearch = view.findViewById<EditText>(R.id.etSearchCustomer)

        adapter = SaleListAdapter(
            onClick = { sale ->
                println(sale)
            },
            onToggleStatus = { sale ->
                if (!sale.isPaid) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Confirmar cambio de estado")
                        .setMessage("¿Deseas marcar esta venta como pagada?")
                        .setPositiveButton("Sí") { _, _ ->
                            viewModel.toggleSaleStatus(sale)
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.salesWithCustomer.observe(viewLifecycleOwner) { sales ->
            adapter.submitList(sales)
        }

        viewModel.loadSalesWithCustomer()

        etSearch.doAfterTextChanged { editable ->
            val query = editable.toString().lowercase()
            val filtered = viewModel.salesWithCustomer.value?.filter {
                it.customerName?.lowercase()?.contains(query) == true
            }
            adapter.submitList(filtered)
        }
    }
}