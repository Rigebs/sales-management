package com.rige.ui

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

class ProductListFragment : Fragment() {

    private lateinit var binding: FragmentProductListBinding
    private val viewModel: ProductViewModel by activityViewModels()
    private lateinit var adapter: ProductListAdapter
    private var allProducts: List<Product> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ProductListAdapter(onEdit = { product ->
            val action = ProductListFragmentDirections.actionToEditProduct(product.id)
            findNavController().navigate(action)
        })

        binding.rvProducts.adapter = adapter
        binding.rvProducts.layoutManager = LinearLayoutManager(requireContext())

        if (viewModel.products.value.isNullOrEmpty()) {
            viewModel.loadProducts()
        }

        // Observar productos
        viewModel.products.observe(viewLifecycleOwner) { products ->
            allProducts = products
            adapter.submitList(products)
        }

        // Filtro de búsqueda
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

        // Botón para agregar nuevo producto
        binding.fabAddProduct.setOnClickListener {
            findNavController().navigate(R.id.actionToEditProduct)
        }
    }
}