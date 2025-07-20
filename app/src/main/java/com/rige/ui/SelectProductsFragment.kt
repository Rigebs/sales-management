package com.rige.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.rige.R
import com.rige.adapters.ProductCardAdapter
import com.rige.models.Category
import com.rige.models.Product
import com.rige.models.extra.ProductFilterOptions
import com.rige.viewmodels.CartViewModel
import com.rige.viewmodels.CategoryViewModel
import com.rige.viewmodels.ProductViewModel

class SelectProductsFragment : Fragment() {

    private val productViewModel: ProductViewModel by activityViewModels()
    private val cartViewModel: CartViewModel by activityViewModels()
    private val categoryViewModel: CategoryViewModel by activityViewModels()

    private lateinit var adapter: ProductCardAdapter
    private var allProducts: List<Product> = emptyList()
    private var allCategories: List<Category> = emptyList()

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var chipGroup: ChipGroup

    private var selectedCategoryId: String? = null
    private var searchQuery: String = ""
    private var isFirstLoad = true
    private var deepSearchTriggered = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_select_products, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.rvProducts)
        searchView = view.findViewById(R.id.searchView)
        chipGroup = view.findViewById(R.id.chipGroupCategories)

        adapter = ProductCardAdapter(
            onAddClicked = { product ->
                cartViewModel.addItemToCart(
                    product.id,
                    product.name,
                    product.isDecimal,
                    product.measureUnit,
                    product.quantity,
                    product.imageUrl ?: "",
                    product.sellingPrice
                )
                Toast.makeText(requireContext(), "${product.name} agregado al carrito", Toast.LENGTH_SHORT).show()
            },
            onDeepSearchClick = {
                deepSearchTriggered = true
                applyFilters()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        setupPagination()

        productViewModel.products.observe(viewLifecycleOwner) { products ->
            allProducts = products
            filterLocally(forceHideDeepSearch = deepSearchTriggered)
            isFirstLoad = false
            deepSearchTriggered = false
        }

        productViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            view.post {
                adapter.showLoading(isLoading)
            }
        }

        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            allCategories = categories
            setupCategoryChips(categories)
        }

        chipGroup.setOnCheckedStateChangeListener { _, _ ->
            val selectedChip = chipGroup.findViewById<Chip>(chipGroup.checkedChipId)
            selectedCategoryId = selectedChip?.tag as? String
            applyFilters()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery = newText?.trim().orEmpty()
                if (searchQuery.isBlank()) {
                    deepSearchTriggered = false
                    applyFilters()
                } else {
                    filterLocally()
                }
                return true
            }
        })

        productViewModel.refreshAndLoad()
        categoryViewModel.loadCategories()
    }

    private fun setupPagination() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    val layoutManager = rv.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

                    val isNearBottom = (visibleItemCount + firstVisibleItem) >= totalItemCount - 1
                    if (isNearBottom && productViewModel.isLoading.value != true) {
                        productViewModel.loadNextPage()
                    }
                }
            }
        })
    }

    private fun setupCategoryChips(categories: List<Category>) {
        chipGroup.removeAllViews()

        val allChip = Chip(requireContext()).apply {
            text = "Todas"
            tag = null
            isCheckable = true
            isChecked = true
        }
        chipGroup.addView(allChip)

        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category.name
                tag = category.id
                isCheckable = true
            }
            chipGroup.addView(chip)
        }
    }

    private fun filterLocally(forceHideDeepSearch: Boolean = false) {
        val filtered = allProducts.filter { product ->
            val matchesQuery = product.name.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryId == null || product.categoryId == selectedCategoryId
            matchesQuery && matchesCategory
        }

        adapter.submitList(filtered)

        val showDeepSearch = !forceHideDeepSearch &&
                searchQuery.isNotBlank() &&
                filtered.size < 10 &&
                productViewModel.hasMore &&
                !deepSearchTriggered

        println("ESTADO: $showDeepSearch")
        println(searchQuery.isNotBlank())
        println(filtered.size < 10)
        println(productViewModel.hasMore)
        println(!deepSearchTriggered)


        adapter.setShowDeepSearchButton(showDeepSearch)
    }

    private fun applyFilters() {
        val filters = ProductFilterOptions(
            categoryId = selectedCategoryId,
            nameContains = searchQuery.takeIf { it.isNotBlank() }
        )
        productViewModel.refreshAndLoad(filters)
        isFirstLoad = true
    }
}
