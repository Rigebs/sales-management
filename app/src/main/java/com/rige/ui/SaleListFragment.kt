package com.rige.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rige.FilterCallback
import com.rige.R
import com.rige.adapters.SaleListAdapter
import com.rige.databinding.FragmentSaleListBinding
import com.rige.models.SaleCustomer
import com.rige.models.extra.FilterOptions
import com.rige.viewmodels.CustomerViewModel
import com.rige.viewmodels.SaleViewModel
import kotlinx.datetime.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoField

class SaleListFragment : Fragment() {

    private lateinit var binding: FragmentSaleListBinding
    private val viewModel: SaleViewModel by activityViewModels()
    private val customerViewModel: CustomerViewModel by activityViewModels()
    private lateinit var adapter: SaleListAdapter

    private var isFirstDataLoad = true
    private var shouldScrollToTop = false
    private var currentFilters = FilterOptions()

    private var currentDateChipId: Int = R.id.chipAllDates

    private var customerId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customerId = arguments?.getString("customerId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSaleListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        customerId?.let { id ->
            currentFilters = currentFilters.copy(customerId = id)
            binding.layoutCustomerInfo.visibility = View.VISIBLE

            customerViewModel.getCustomerById(id).observe(viewLifecycleOwner) { customer ->
                val fullName = buildString {
                    append(customer?.name)
                    if (!customer?.paternalSurname.isNullOrBlank()) append(" ${customer?.paternalSurname}")
                    if (!customer?.maternalSurname.isNullOrBlank()) append(" ${customer?.maternalSurname}")
                }
                binding.tvCustomerName.text = "Cliente: $fullName"
            }
        }

        setupRecyclerView()
        setupObservers()
        refreshList()
        setupFilters()
        setupDateFilters()

        viewModel.ensureInitialDataLoaded()
    }
    private fun setupRecyclerView() {
        adapter = SaleListAdapter { saleId ->
            navigateToSaleDetails(saleId)
        }

        binding.rvSales.apply {
            adapter = this@SaleListFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
            addOnScrollListener(paginationScrollListener())
        }
    }

    private fun setupObservers() {
        observeSales()
        observeLoadingState()
        viewModel.totalSales.observe(viewLifecycleOwner) { total ->
            binding.tvTotalAmount.text = "Total: S/: ${"%.2f".format(total)}"
            binding.tvTotalAmount.visibility = View.VISIBLE
        }

    }

    private fun observeSales() {
        viewModel.sales.observe(viewLifecycleOwner) { sales ->
            adapter.submitSales(sales)
            toggleEmptyState(sales)

            if ((isFirstDataLoad || shouldScrollToTop) && sales.isNotEmpty()) {
                binding.rvSales.scrollToPosition(0)
                isFirstDataLoad = false
                shouldScrollToTop = false
            }
        }
    }

    private fun observeLoadingState() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.rvSales.post {
                adapter.showLoading(isLoading)
            }
        }
    }

    private fun setupFilters() {
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            applyChipFilter(checkedIds.firstOrNull())
        }

        binding.btnMoreFilters.setOnClickListener {
            showAdvancedFilterDialog()
        }

        applyChipFilter(binding.chipGroup.checkedChipId)
    }

    private fun applyChipFilter(chipId: Int?) {
        val isPaid = when (chipId) {
            R.id.chipAll -> null
            R.id.chipPaid -> true
            R.id.chipUnpaid -> false
            else -> null
        }

        currentFilters = currentFilters.copy(isPaid = isPaid, customerId = currentFilters.customerId)
        refreshList()

        // ✅ Solo recalcular total si estamos en chip de fecha "válido"
        if (currentDateChipId != R.id.chipAllDates) {
            viewModel.fetchTotalSales(currentFilters)
        }
    }

    private fun applyAdvancedFilters(newFilters: FilterOptions) {
        // Mantener el filtro de isPaid actual
        currentFilters = newFilters.copy(isPaid = currentFilters.isPaid, customerId = currentFilters.customerId)
        refreshList()
    }

    private fun refreshList() {
        println("FRSGMENT: $currentFilters")
        shouldScrollToTop = true
        viewModel.refreshAndLoad(currentFilters)
    }

    private fun toggleEmptyState(sales: List<SaleCustomer>) {
        val isLoading = viewModel.isLoading.value == true
        val isEmpty = sales.isEmpty() && !isLoading

        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvSales.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun paginationScrollListener() = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dy <= 0) return

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
            val totalItemCount = layoutManager.itemCount

            val shouldLoadMore = viewModel.isLoading.value != true &&
                    lastVisibleItem >= totalItemCount - 1

            if (shouldLoadMore) {
                viewModel.loadNextPage()
            }
        }
    }

    private fun showAdvancedFilterDialog() {
        val bottomSheet = MoreFiltersBottomSheetFragment().apply {
            setInitialFilters(currentFilters)
            filterCallback = object : FilterCallback {
                override fun onFiltersApplied(filters: FilterOptions) {
                    applyAdvancedFilters(filters)
                }
            }
        }
        bottomSheet.show(parentFragmentManager, "MoreFiltersBottomSheet")
    }

    private fun navigateToSaleDetails(saleId: String) {
        val bundle = bundleOf("saleId" to saleId)
        findNavController().navigate(R.id.action_saleListFragment_to_saleDetailsFragment, bundle)
    }

    private fun setupDateFilters() {
        binding.chipDateGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val selectedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener

            val today = LocalDate.now()
            val newFilters = when (selectedId) {
                R.id.chipAllDates -> {
                    currentFilters.copy(dateFrom = null, dateTo = null, customerId = currentFilters.customerId)
                }
                R.id.chipToday -> {
                    currentFilters.copy(dateFrom = today, dateTo = today, customerId = currentFilters.customerId)
                }
                R.id.chipWeek -> {
                    val startOfWeek = today.minusDays((today.dayOfWeek.value - 1).toLong())
                    val endOfWeek = today.plusDays((7 - today.dayOfWeek.value).toLong())
                    currentFilters.copy(dateFrom = startOfWeek, dateTo = endOfWeek, customerId = currentFilters.customerId)
                }
                R.id.chipMonth -> {
                    val startOfMonth = today.withDayOfMonth(1)
                    val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())
                    currentFilters.copy(dateFrom = startOfMonth, dateTo = endOfMonth, customerId = currentFilters.customerId)
                }
                else -> currentFilters
            }

            currentFilters = newFilters
            currentDateChipId = selectedId

            refreshList()

            // ✅ Solo ejecutamos fetchTotalSales si no es "Todos"
            if (selectedId != R.id.chipAllDates) {
                viewModel.fetchTotalSales(currentFilters)
            } else {
                binding.tvTotalAmount.visibility = View.GONE // Ocultar total si es "Todos"
            }
        }

        // Seleccionar chip "Todos" al inicio
        binding.chipDateGroup.check(R.id.chipAllDates)
    }
}