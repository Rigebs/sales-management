package com.rige.ui

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rige.R
import com.rige.adapters.SaleListAdapter
import com.rige.databinding.FragmentSaleListBinding
import com.rige.viewmodels.SaleViewModel

class SaleListFragment : Fragment() {

    private lateinit var binding: FragmentSaleListBinding
    private val viewModel: SaleViewModel by activityViewModels()
    private lateinit var adapter: SaleListAdapter

    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSaleListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SaleListAdapter(
            sales = mutableListOf(),
            onClick = { sale -> println("🔍 Click en: ${sale.id}") },
            onToggleStatus = { sale ->
                if (!sale.isPaid) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Confirmar cambio de estado")
                        .setMessage("¿Deseas marcar esta venta como pagada?")
                        .setPositiveButton("Sí") { _, _ -> viewModel.toggleSaleStatus(sale) }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            }
        )

        binding.rvSales.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSales.adapter = adapter

        setupObservers()
        setupFilters()
        setupScrollListener()

        binding.chipAll.isChecked = true
        viewModel.resetAndLoad()
    }

    private fun setupObservers() {
        viewModel.pagedSales.observe(viewLifecycleOwner) {
            adapter.updateSalesWithDiff(it)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { adapter.showLoadingFooter(it) }
        viewModel.error.observe(viewLifecycleOwner) {
            it?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun setupFilters() {
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val isPaid = when (checkedIds.firstOrNull()) {
                R.id.chipPaid -> true
                R.id.chipUnpaid -> false
                else -> null
            }
            viewModel.resetAndLoadWithLocalFilter(binding.searchCustomer.text.toString(), isPaid)
        }

        binding.searchCustomer.doAfterTextChanged { text ->
            searchRunnable?.let { searchHandler.removeCallbacks(it) }
            searchRunnable = Runnable {
                viewModel.resetAndLoadWithLocalFilter(text?.toString().orEmpty(), viewModel.currentIsPaidFilter)
            }
            searchHandler.postDelayed(searchRunnable!!, 500)
        }
    }

    private fun setupScrollListener() {
        binding.rvSales.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)

                if (dy <= 0) return

                val layoutManager = rv.layoutManager as LinearLayoutManager
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (lastVisible + 3 >= totalItemCount) {
                    viewModel.loadNextPage()
                }
            }
        })
    }
}