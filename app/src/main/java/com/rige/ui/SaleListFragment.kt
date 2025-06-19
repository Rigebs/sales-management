package com.rige.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
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
    private var isFirstDataLoad = true

    private var currentFilter: Boolean? = null
    private var shouldScrollToTop = false


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
        viewModel.ensureInitialDataLoaded()
        setupFilter()
    }

    private fun setupRecyclerView() {
        adapter = SaleListAdapter()
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
                            val searchText = binding.searchCustomer.text?.toString().orEmpty()
                            viewModel.loadNextPage(
                                searchQuery = searchText.takeIf { it.isNotBlank() },
                                isPaid = currentFilter
                            )
                        }
                    }
                }
            })
        }
    }

    private fun observeViewModel() {
        viewModel.sales.observe(viewLifecycleOwner) { sales ->
            adapter.submitSales(sales)

            if ((isFirstDataLoad || shouldScrollToTop) && sales.isNotEmpty()) {
                binding.rvSales.scrollToPosition(0)
                isFirstDataLoad = false
                shouldScrollToTop = false
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            adapter.showLoading(isLoading)
        }
    }

    private fun setupFilter() {
        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            applyFilter(checkedIds.firstOrNull())
        }

        binding.searchCustomer.doOnTextChanged { text, _, _, _ ->
            applySearchAndFilter()
        }

        applyFilter(binding.chipGroup.checkedChipId)
    }

    private fun applyFilter(chipId: Int?) {
        currentFilter = when (chipId) {
            R.id.chipAll -> null
            R.id.chipPaid -> true
            R.id.chipUnpaid -> false
            else -> null
        }
        applySearchAndFilter()
    }

    private fun applySearchAndFilter() {
        shouldScrollToTop = true
        val searchText = binding.searchCustomer.text?.toString().orEmpty()

        viewModel.refreshAndLoad(
            searchQuery = searchText.takeIf { it.isNotBlank() },
            isPaid = currentFilter
        )
    }
}