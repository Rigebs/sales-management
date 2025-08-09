package com.rige.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.rige.R
import com.rige.adapters.ProductListAdapter
import com.rige.databinding.FragmentProductListBinding
import com.rige.models.Category
import com.rige.models.Product
import com.rige.models.extra.ProductFilterOptions
import com.rige.utils.createCategoryChip
import com.rige.utils.createStatusChip
import com.rige.viewmodels.CategoryViewModel
import com.rige.viewmodels.ProductViewModel
import android.os.Handler
import android.os.Looper

class ProductListFragment : Fragment() {

    private lateinit var binding: FragmentProductListBinding
    private val viewModel: ProductViewModel by activityViewModels()
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private lateinit var adapter: ProductListAdapter

    private var allProducts: List<Product> = emptyList()
    private var categories: List<Category> = emptyList()

    private var selectedCategoryId: String? = null
    private var selectedStatus: String = "Todos"
    private var searchQuery: String = ""

    private var isFirstDataLoad = true
    private var shouldScrollToTop = false
    private var productsLoaded = false
    private var categoriesLoaded = false

    private var searchRunnable: Runnable? = null
    private val searchHandler = Handler(Looper.getMainLooper())

    private var lastScrollPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupAdapter()
        setupRecyclerView()
        setupObservers()
        setupUIActions()

        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY


        viewModel.refreshAndLoad()
        categoryViewModel.loadCategories()
    }

    private fun setupAdapter() {
        adapter = ProductListAdapter(
            onEdit = { product ->
                val action = ProductListFragmentDirections.actionToEditProduct(product.id)
                findNavController().navigate(action)
            },
            onStatusClick = { product -> showStatusConfirmationDialog(product) }
        )
        binding.rvProducts.adapter = adapter
    }

    private fun setupRecyclerView() {
        binding.rvProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = rv.layoutManager as LinearLayoutManager
                lastScrollPosition = layoutManager.findFirstVisibleItemPosition()

                if (dy > 0) {
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 1 &&
                        viewModel.isLoading.value != true
                    ) {
                        viewModel.loadNextPage()
                    }
                }
            }
        })

    }

    private fun setupObservers() {
        categoryViewModel.categories.observe(viewLifecycleOwner) { cats ->
            categories = cats
            categoriesLoaded = true
            loadCategoryChips(cats)
        }

        viewModel.products.observe(viewLifecycleOwner) { products ->
            allProducts = products
            filterLocally()

            if (isFirstDataLoad || shouldScrollToTop) {
                binding.rvProducts.scrollToPosition(0)
                isFirstDataLoad = false
                shouldScrollToTop = false
            } else {
                binding.rvProducts.scrollToPosition(lastScrollPosition)
            }

            productsLoaded = true
        }


        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.rvProducts.post { adapter.showLoading(isLoading) }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (!errorMsg.isNullOrBlank()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupUIActions() {
        setupStatusChips()
        binding.chipGroupStatus.setOnCheckedStateChangeListener { _, _ ->
            val chipId = binding.chipGroupStatus.checkedChipId
            val chip = chipId.takeIf { it != View.NO_ID }
                ?.let { binding.chipGroupStatus.findViewById<Chip>(it) }
            selectedStatus = chip?.text?.toString() ?: "Todos"
            println("Primero")
            applyFilters()
        }

        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, _ ->
            val chipId = binding.chipGroupCategories.checkedChipId
            val chip = chipId.takeIf { it != View.NO_ID }
                ?.let { binding.chipGroupCategories.findViewById<Chip>(it) }
            val categoryName = chip?.text?.toString()
            selectedCategoryId = categories.find { it.name == categoryName }?.id
            if (categoryName == "Todos") selectedCategoryId = null
            println("Segundo")
            applyFilters()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val newQuery = newText ?: ""
                if (newQuery == searchQuery) return true // no hacer nada si es el mismo texto

                searchQuery = newQuery

                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                searchRunnable = Runnable { applyFilters() }
                searchHandler.postDelayed(searchRunnable!!, 500)
                return true
            }
        })

        binding.fabAddProduct.setOnClickListener {
            findNavController().navigate(R.id.actionToEditProduct)
        }
    }

    private fun setupStatusChips() {
        binding.chipGroupStatus.removeAllViews()
        listOf("Todos", "Activos", "Inactivos").forEachIndexed { i, estado ->
            val chip = createStatusChip(requireContext(), estado, isChecked = i == 0)
            binding.chipGroupStatus.addView(chip)
        }
    }

    private fun loadCategoryChips(categories: List<Category>) {
        binding.chipGroupCategories.removeAllViews()
        binding.chipGroupCategories.addView(createCategoryChip(requireContext(), "Todos", isChecked = true))
        categories.forEach { cat ->
            binding.chipGroupCategories.addView(createCategoryChip(requireContext(), cat.name))
        }
    }

    private fun applyFilters() {
        val filters = ProductFilterOptions(
            categoryId = selectedCategoryId,
            isActive = when (selectedStatus) {
                "Activos" -> true
                "Inactivos" -> false
                else -> null
            },
            nameContains = searchQuery.takeIf { it.isNotBlank() }
        )
        viewModel.refreshAndLoad(filters)
        shouldScrollToTop = true
    }

    private fun filterLocally() {
        val filtered = allProducts.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
        adapter.submitList(filtered)
    }

    private fun showStatusConfirmationDialog(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle("Cambiar estado")
            .setMessage("¿Estás seguro de que deseas cambiar el estado de este producto?")
            .setPositiveButton("Sí") { _, _ -> viewModel.toggleStatus(product) }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}