package com.rige.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rige.FilterCallback
import com.rige.R
import com.rige.adapters.SaleListAdapter
import com.rige.databinding.FragmentSaleListBinding
import com.rige.models.extra.FilterOptions
import com.rige.viewmodels.SaleViewModel

class SaleListFragment : Fragment() {

    private lateinit var binding: FragmentSaleListBinding
    private val viewModel: SaleViewModel by activityViewModels()
    private lateinit var adapter: SaleListAdapter
    private var isFirstDataLoad = true

    private var shouldScrollToTop = false

    private var currentFilters = FilterOptions()

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
        adapter = SaleListAdapter { saleId: String ->
            val bundle = bundleOf("saleId" to saleId)
            findNavController().navigate(
                R.id.action_saleListFragment_to_saleDetailsFragment,
                bundle
            )
        }

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

            val isLoading = viewModel.isLoading.value == true
            val isEmpty = sales.isEmpty() && !isLoading

            binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvSales.visibility = if (isEmpty) View.GONE else View.VISIBLE

            if ((isFirstDataLoad || shouldScrollToTop) && sales.isNotEmpty()) {
                binding.rvSales.scrollToPosition(0)
                isFirstDataLoad = false
                shouldScrollToTop = false
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.rvSales.post {
                adapter.showLoading(isLoading)
            }
        }
    }

    private fun setupFilter() {
        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            applyFilter(checkedIds.firstOrNull())
        }

        binding.btnMoreFilters.setOnClickListener {
            val bottomSheet = MoreFiltersBottomSheetFragment()
            bottomSheet.setInitialFilters(currentFilters)
            bottomSheet.filterCallback = object : FilterCallback {
                override fun onFiltersApplied(filters: FilterOptions) {
                    applyAdvancedFilters(filters)
                }
            }
            bottomSheet.show(parentFragmentManager, "MoreFiltersBottomSheet")

        }

        applyFilter(binding.chipGroup.checkedChipId)
    }

    private fun applyFilter(chipId: Int?) {
        val isPaid = when (chipId) {
            R.id.chipAll -> null
            R.id.chipPaid -> true
            R.id.chipUnpaid -> false
            else -> null
        }

        currentFilters = currentFilters.copy(isPaid = isPaid)
        applySearchAndFilter()
    }

    private fun applySearchAndFilter() {
        shouldScrollToTop = true
        viewModel.refreshAndLoad(currentFilters)
    }

    private fun applyAdvancedFilters(advancedFilters: FilterOptions) {
        // Mantén el valor de isPaid del chip actual
        val mergedFilters = advancedFilters.copy(isPaid = currentFilters.isPaid)

        currentFilters = mergedFilters
        shouldScrollToTop = true
        viewModel.refreshAndLoad(currentFilters)
    }
}