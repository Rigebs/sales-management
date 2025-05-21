package com.rige.ui

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.android.material.chip.Chip
import com.rige.models.Category
import com.rige.viewmodels.CategoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
        })

        binding.rvProducts.adapter = adapter
        binding.rvProducts.layoutManager = LinearLayoutManager(requireContext())

        categoryViewModel.loadCategories()

        if (viewModel.products.value.isNullOrEmpty()) {
            viewModel.loadProducts()
        }

        viewModel.products.observe(viewLifecycleOwner) { products ->
            allProducts = products
            adapter.submitList(products)
        }

        categoryViewModel.categories.observe(viewLifecycleOwner) { cats ->
            categories = cats
            loadCategoryChips(cats)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = allProducts.filter {
                    it.name.contains(newText ?: "", ignoreCase = true) ||
                            (it.barCode?.contains(newText ?: "", ignoreCase = true) ?: false)
                }
                adapter.submitList(filtered)
                return true
            }
        })

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
                applyFilters()
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
        val result = allProducts.filter { product ->
            val coincideCategoria = selectedCategoryId == null || product.categoryId == selectedCategoryId
            val coincideEstado = when (selectedStatus) {
                "Activos" -> product.status
                "Inactivos" -> !product.status
                else -> true
            }
            val coincideBusqueda = product.name.contains(searchQuery, ignoreCase = true) ||
                    (product.barCode?.contains(searchQuery, ignoreCase = true) ?: false)

            coincideCategoria && coincideEstado && coincideBusqueda
        }

        adapter.submitList(result)
    }
}