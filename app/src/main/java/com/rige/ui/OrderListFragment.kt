package com.rige.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rige.R
import com.rige.adapters.OrderListAdapter
import com.rige.databinding.FragmentOrderListBinding
import com.rige.models.extra.OrderFilterOptions
import com.rige.viewmodels.OrderViewModel
import org.threeten.bp.LocalDate

class OrderListFragment : Fragment() {

    private lateinit var binding: FragmentOrderListBinding
    private lateinit var adapter: OrderListAdapter
    private val viewModel: OrderViewModel by activityViewModels()

    private var isLoading = false
    private var currentFilters: OrderFilterOptions = OrderFilterOptions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = OrderListAdapter(
            onOrderClick = { order ->
                val bundle = bundleOf("orderId" to order.id)
                findNavController().navigate(
                    R.id.action_orderListFragment_to_orderDetailsFragment,
                    bundle
                )
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        setupScrollListener()
        observeViewModel()

        binding.btnFilter.setOnClickListener {
            val filterSheet = OrderFiltersBottomSheetFragment { startDate, endDate ->
                applyFilters(startDate, endDate)
            }
            filterSheet.show(parentFragmentManager, "OrderFiltersBottomSheet")
        }

        // Primera carga
        viewModel.refreshAndLoad()
    }

    private fun setupScrollListener() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && viewModel.hasMore) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3) {
                        viewModel.loadNextPage()
                    }
                }
            }
        })
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            isLoading = loading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.orders.observe(viewLifecycleOwner) { orders ->
            if (orders.isEmpty()) {
                binding.recyclerView.visibility = View.GONE
            } else {
                adapter.submitList(orders.toMutableList())
                binding.recyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun applyFilters(startDate: LocalDate?, endDate: LocalDate?) {
        currentFilters = OrderFilterOptions(
            dateFrom = startDate,
            dateTo = endDate
        )
        adapter.submitList(emptyList()) // limpia lista
        viewModel.refreshAndLoad(currentFilters)
    }
}