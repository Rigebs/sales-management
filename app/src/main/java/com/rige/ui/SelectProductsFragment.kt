package com.rige.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rige.R
import com.rige.adapters.ProductCardAdapter
import com.rige.clients.SupabaseClient
import com.rige.repositories.ProductRepository
import com.rige.viewmodels.ProductViewModel

class SelectProductsFragment : Fragment() {

    private lateinit var viewModel: ProductViewModel
    private lateinit var adapter: ProductCardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_select_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val repo = ProductRepository(SupabaseClient.supabase)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ProductViewModel(repo) as T
            }
        })[ProductViewModel::class.java]

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvProducts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.products.observe(viewLifecycleOwner) { products ->
            adapter = ProductCardAdapter(products) { product ->
                (activity as? MakeSaleActivity)?.increaseCartCount()            }
            recyclerView.adapter = adapter
        }

        viewModel.loadProducts()
    }
}