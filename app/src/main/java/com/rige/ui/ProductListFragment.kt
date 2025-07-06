package com.rige.ui

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rige.R
import com.rige.adapters.ProductListAdapter
import com.rige.databinding.FragmentProductListBinding
import com.rige.models.Product
import com.rige.viewmodels.ProductViewModel
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.rige.models.Category
import com.rige.models.extra.ProductFilterOptions
import com.rige.viewmodels.CategoryViewModel

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

    private var productsLoaded = false
    private var categoriesLoaded = false

    private var isFirstDataLoad = true
    private var shouldScrollToTop = false

    private var deepSearchTriggered = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ProductListAdapter(
            onEdit = { product ->
            val action = ProductListFragmentDirections.actionToEditProduct(product.id)
            findNavController().navigate(action)
        },
            onStatusClick = { product ->
            showStatusConfirmationDialog(product)
        },
            onDeepSearchClick = {
                deepSearchTriggered = true
                applyFilters()
            })

        binding.rvProducts.adapter = adapter
        binding.rvProducts.layoutManager = LinearLayoutManager(requireContext())

        categoryViewModel.loadCategories()

        setupRecyclerView()
        observeViewModel()

        if (viewModel.products.value.isNullOrEmpty()) {
            viewModel.refreshAndLoad()
        }

        categoryViewModel.categories.observe(viewLifecycleOwner) { cats ->
            categories = cats
            categoriesLoaded = true
            loadCategoryChips(cats)
            checkIfReadyToShowContent()
        }

        listOf("Todos", "Activos", "Inactivos").forEachIndexed { i, estado ->
            val chip = Chip(requireContext()).apply {
                text = estado
                isCheckable = true
                isClickable = true
                id = View.generateViewId()
                if (i == 0) isChecked = true
                when (estado) {
                    "Todos" -> chipIcon = ContextCompat.getDrawable(context, R.drawable.ic_gray_circle)
                    "Activos" -> chipIcon = ContextCompat.getDrawable(context, R.drawable.ic_green_circle)
                    "Inactivos" -> chipIcon = ContextCompat.getDrawable(context, R.drawable.ic_red_circle)
                }
                isChipIconVisible = chipIcon != null
                chipIconSize = 24f
                chipIconTint = null
            }
            binding.chipGroupStatus.addView(chip)
        }

        binding.chipGroupStatus.setOnCheckedStateChangeListener { _, _ ->
            val chipId = binding.chipGroupStatus.checkedChipId
            val chip = chipId.takeIf { it != View.NO_ID }
                ?.let { binding.chipGroupStatus.findViewById<Chip>(it) }
            selectedStatus = chip?.text?.toString() ?: "Todos"
            applyFilters()
        }

        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, _ ->
            val chipId = binding.chipGroupCategories.checkedChipId
            val chip = chipId.takeIf { it != View.NO_ID }
                ?.let { binding.chipGroupCategories.findViewById<Chip>(it) }
            val categoryName = chip?.text?.toString()
            selectedCategoryId = categories.find { it.name == categoryName }?.id
            if (categoryName == "Todos") selectedCategoryId = null
            applyFilters()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery = newText ?: ""

                if (searchQuery.isBlank()) {
                    deepSearchTriggered = false
                    applyFilters()
                } else {
                    filterLocally()
                }

                return true
            }
        })

        binding.fabAddProduct.setOnClickListener {
            findNavController().navigate(R.id.actionToEditProduct)
        }
    }

    private fun loadCategoryChips(categories: List<Category>) {
        binding.chipGroupCategories.removeAllViews()

        val chipTodos = Chip(requireContext()).apply {
            text = "Todos"
            isCheckable = true
            isClickable = true
            isChecked = true
            id = View.generateViewId()
        }
        binding.chipGroupCategories.addView(chipTodos)

        categories.forEach { cat ->
            val chip = Chip(requireContext()).apply {
                text = cat.name
                isCheckable = true
                isClickable = true
                id = View.generateViewId()
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    private fun showStatusConfirmationDialog(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle("Cambiar estado")
            .setMessage("¿Estás seguro de que deseas cambiar el estado de este producto?")
            .setPositiveButton("Sí") { _, _ ->
                viewModel.toggleStatus(product)
            }
            .setNegativeButton("Cancelar", null)
            .show()
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

    private fun checkIfReadyToShowContent() {
        if (productsLoaded && categoriesLoaded) {
            binding.progressBar.visibility = View.GONE
            binding.contentContainer.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.VISIBLE
            binding.contentContainer.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        binding.rvProducts.apply {
            adapter = this@ProductListFragment.adapter
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
        viewModel.products.observe(viewLifecycleOwner) { products ->
            allProducts = products
            adapter.submitList(products)

            if ((isFirstDataLoad || shouldScrollToTop) && products.isNotEmpty()) {
                binding.rvProducts.scrollToPosition(0)
                isFirstDataLoad = false
                shouldScrollToTop = false
            }

            productsLoaded = true
            checkIfReadyToShowContent()

            filterLocally(forceHideDeepSearch = deepSearchTriggered)
            deepSearchTriggered = false
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.rvProducts.post {
                adapter.showLoading(isLoading)
            }

            if (isLoading && viewModel.products.value.isNullOrEmpty()) {
                binding.progressBar.visibility = View.VISIBLE
                binding.contentContainer.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.contentContainer.visibility = View.VISIBLE
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (!errorMsg.isNullOrBlank()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun filterLocally(forceHideDeepSearch: Boolean = false) {
        val filtered = allProducts.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }

        adapter.submitList(filtered)

        adapter.setShowDeepSearchButton(
            !forceHideDeepSearch &&
                    searchQuery.isNotBlank() &&
                    filtered.size < 10 &&
                    viewModel.hasMore &&
                    !deepSearchTriggered
        )
    }
}