package com.rige.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rige.R
import com.rige.adapters.SaleAdapter
import com.rige.databinding.FragmentSaleListBinding
import com.rige.viewmodels.SaleViewModel2

class SaleListFragment : Fragment() {

    private lateinit var binding: FragmentSaleListBinding
    private val viewModel: SaleViewModel2 by activityViewModels()
    private lateinit var adapter: SaleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSaleListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        observeViewModel()

        if (viewModel.sales.value.isNullOrEmpty()) {
            viewModel.loadNextPage()
        }
    }

    private fun setupRecyclerView() {
        adapter = SaleAdapter()
        binding.rvSales.apply {
            adapter = this@SaleListFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0) {
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val visibleItemCount = layoutManager.childCount
                        val totalItemCount = layoutManager.itemCount
                        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                        val isLastItemVisible =
                            (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 1

                        if (viewModel.isLoading.value != true && isLastItemVisible) {
                            viewModel.loadNextPage()
                        }
                    }
                }
            })
        }
    }

    private fun observeViewModel() {
        viewModel.sales.observe(viewLifecycleOwner) { sales ->
            adapter.submitSales(sales)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            (activity as? SalesActivity)?.showProgressBarInActionBar(isLoading)
        }
    }
}