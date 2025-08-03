package com.rige.ui

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.rige.R
import com.rige.adapters.SelectProductPurchaseAdapter
import com.rige.databinding.FragmentSelectProductsPurchaseBinding
import com.rige.models.Product
import com.rige.models.extra.ProductFilterOptions
import com.rige.viewmodels.CategoryViewModel
import com.rige.viewmodels.PurchaseViewModel
import com.rige.viewmodels.ProductViewModel
import java.math.BigDecimal

class SelectProductsPurchaseFragment : Fragment() {

    private lateinit var binding: FragmentSelectProductsPurchaseBinding

    private val productViewModel: ProductViewModel by activityViewModels()
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private val purchaseViewModel: PurchaseViewModel by activityViewModels()

    private lateinit var productAdapter: SelectProductPurchaseAdapter

    private var isLoading = false

    private var currentQuery: String? = null

    private var selectedCategoryId: String? = null
    private var selectedStatus: String = "Todos"

    private var hasScrolledToTop = false

    private var allProducts: List<Product> = emptyList()
    private var deepSearchTriggered = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectProductsPurchaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeProducts()
        setupSearchView()

        categoryViewModel.loadCategories()
        observeCategories()
        setupStatusChips()

        productViewModel.refreshAndLoad(ProductFilterOptions())
    }

    override fun onResume() {
        super.onResume()
        hasScrolledToTop = false
    }

    private fun setupRecyclerView() {
        productAdapter = SelectProductPurchaseAdapter(
            onAddToPurchaseClick = { product ->
                showQuantityDialog(product)
            },
            onDeepSearchClick = {
                deepSearchTriggered = true
                refreshList()
            }
        )

        val layoutManager = LinearLayoutManager(requireContext())

        binding.rvProducts.apply {
            this.layoutManager = layoutManager
            adapter = productAdapter

            // Scroll listener para paginación
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(rv, dx, dy)

                    if (deepSearchTriggered || currentQuery.orEmpty().isNotBlank()) return

                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                    if (!isLoading && productViewModel.hasMore && lastVisibleItem >= totalItemCount - 3) {
                        productViewModel.loadNextPage()
                    }
                }
            })
        }
    }

    private fun observeProducts() {
        productViewModel.products.observe(viewLifecycleOwner) { productList ->
            allProducts = productList

            if (deepSearchTriggered) {
                productAdapter.updateList(productList, showFooter = false)
                productAdapter.updateDeepSearchButton(false)
                deepSearchTriggered = false
            } else {
                filterLocally(forceHideDeepSearch = false)
            }

            deepSearchTriggered = false
        }

        productViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            isLoading = loading

            view?.post {
                // Mostrar footer de carga si está cargando y no es una búsqueda profunda
                productAdapter.updateList(
                    allProducts,
                    showFooter = loading && !deepSearchTriggered && productViewModel.hasMore
                )

                productAdapter.updateDeepSearchButton(
                    !deepSearchTriggered &&
                            currentQuery.orEmpty().isNotBlank() &&
                            allProducts.size < 10 &&
                            productViewModel.hasMore
                )
            }
        }

        productViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText.orEmpty()

                println("Filtrado local: $currentQuery") // debug log

                if (currentQuery!!.isBlank()) {
                    deepSearchTriggered = false
                    filterLocally(forceHideDeepSearch = true)
                } else {
                    filterLocally()
                }

                return true
            }


            override fun onQueryTextSubmit(query: String?): Boolean {
                currentQuery = query
                deepSearchTriggered = true
                refreshList() // ← aquí sí hacés el fetch a Supabase
                return true
            }
        })
    }

    private fun refreshList() {
        val onlyActive = when (selectedStatus) {
            "Activos" -> true
            "Inactivos" -> false
            else -> null
        }

        productViewModel.refreshAndLoad(
            ProductFilterOptions(
                nameContains = currentQuery,
                categoryId = selectedCategoryId,
                isActive = onlyActive
            )
        )
    }

    private fun observeCategories() {
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            binding.chipGroupCategories.removeAllViews()

            // Chip "Todos"
            val allChip = Chip(requireContext()).apply {
                text = "Todos"
                isCheckable = true
                isClickable = true
                isChecked = true
                id = View.generateViewId()
                tag = null
            }
            binding.chipGroupCategories.addView(allChip)

            categories.forEach { category ->
                val chip = Chip(requireContext()).apply {
                    text = category.name
                    isCheckable = true
                    isClickable = true
                    id = View.generateViewId()
                    tag = category.id
                }
                binding.chipGroupCategories.addView(chip)
            }

            binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
                val selectedId = checkedIds.firstOrNull()
                val selectedChip = selectedId?.let { group.findViewById<Chip>(it) }
                selectedCategoryId = selectedChip?.tag as? String
                refreshList()
            }
        }

        categoryViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), "Error cargando categorías: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupStatusChips() {
        val estados = listOf("Todos", "Activos", "Inactivos")

        estados.forEachIndexed { i, estado ->
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

        binding.chipGroupStatus.setOnCheckedStateChangeListener { group, checkedIds ->
            val selectedId = checkedIds.firstOrNull()
            val selectedChip = selectedId?.let { group.findViewById<Chip>(it) }
            selectedStatus = selectedChip?.text?.toString() ?: "Todos"
            refreshList()
        }
    }

    private fun filterLocally(forceHideDeepSearch: Boolean = false) {
        val query = currentQuery.orEmpty()

        val filtered = allProducts.filter {
            it.name.contains(query, ignoreCase = true)
        }

        productAdapter.updateList(filtered)

        productAdapter.updateDeepSearchButton(
            !forceHideDeepSearch &&
                    query.isNotBlank() &&
                    filtered.size < 10 &&
                    productViewModel.hasMore &&
                    !deepSearchTriggered
        )
    }

    private fun showQuantityDialog(product: Product) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom_input, null)
        val inputEditText = dialogView.findViewById<TextInputEditText>(R.id.inputEditText)
        val chipGroup = dialogView.findViewById<ChipGroup>(R.id.chipGroup)

        // Ajustar inputType según si es decimal
        inputEditText.inputType = if (product.isDecimal)
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        else
            InputType.TYPE_CLASS_NUMBER

        // Crear sugerencias según tipo
        val suggestions = if (product.isDecimal) {
            listOf(
                "600 ${product.measureUnit}",
                "800 ${product.measureUnit}",
                "1000 ${product.measureUnit}"
            )
        } else {
            listOf("12 u", "50 u", "100 u")
        }

        suggestions.forEach { suggestion ->
            val chip = Chip(requireContext()).apply {
                text = suggestion
                isClickable = true
                isCheckable = false
                setOnClickListener {
                    inputEditText.setText(suggestion.filter { it.isDigit() || it == '.' })
                }
            }
            chipGroup.addView(chip)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Cantidad para ${product.name}")
            .setView(dialogView)
            .setPositiveButton("Aceptar") { dialog, _ ->
                val inputValue = inputEditText.text.toString()
                if (inputValue.isNotBlank()) {
                    try {
                        val quantity = BigDecimal(inputValue)
                        purchaseViewModel.addItemToPurchase(
                            productId = product.id,
                            name = product.name,
                            isDecimal = product.isDecimal,
                            measureUnit = product.measureUnit,
                            stock = product.quantity,
                            imageUrl = product.imageUrl.toString(),
                            unitPrice = product.costPrice ?: BigDecimal.ZERO,
                            quantityToAdd = quantity
                        )
                    } catch (e: NumberFormatException) {
                        Toast.makeText(requireContext(), "Valor inválido", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}