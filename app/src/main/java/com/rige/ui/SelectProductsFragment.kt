package com.rige.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.rige.viewmodels.CartViewModel
import com.rige.viewmodels.CategoryViewModel
import com.rige.viewmodels.ProductViewModel

class SelectProductsFragment : Fragment() {

    private val productViewModel: ProductViewModel by activityViewModels()
    private val cartViewModel: CartViewModel by activityViewModels()
    private val categoryViewModel: CategoryViewModel by activityViewModels()

    private lateinit var adapter: ProductCardAdapter
    private lateinit var allProducts: List<Product>
    private lateinit var allCategories: List<Category>

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var chipGroup: ChipGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_select_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.rvProducts)
        searchView = view.findViewById(R.id.searchView)
        chipGroup = view.findViewById(R.id.chipGroupCategories)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ProductCardAdapter { product ->
            cartViewModel.addItemToCart(
                product.id,
                product.name,
                product.imageUrl.toString(),
                product.sellingPrice
            )
        }
        recyclerView.adapter = adapter

        recyclerView.adapter = adapter

        // Observa productos
        productViewModel.products.observe(viewLifecycleOwner) { products ->
            allProducts = products
            filterProducts()
        }

        // Observa categorías
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            allCategories = categories
            setupCategoryChips(categories)
        }

        // Eventos
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                filterProducts()
                return true
            }
        })

        chipGroup.setOnCheckedChangeListener { _, _ -> filterProducts() }

        // Cargar datos
        productViewModel.loadProducts()
        categoryViewModel.loadCategories()
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

    private fun filterProducts() {
        val query = searchView.query?.toString()?.lowercase()?.trim() ?: ""
        val selectedChip = chipGroup.findViewById<Chip>(chipGroup.checkedChipId)
        val selectedCategoryId = selectedChip?.tag as? String


        val filtered = allProducts.filter { product ->
            val matchesQuery = product.name.lowercase().contains(query)
            val matchesCategory = selectedCategoryId == null || product.categoryId == selectedCategoryId
            matchesQuery && matchesCategory
        }

        adapter.submitList(filtered)
    }
}