package com.rige.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rige.R
import com.rige.adapters.CartAdapter
import com.rige.viewmodels.CartViewModel
import java.math.BigDecimal

class GenerateSaleFragment : Fragment() {

    private val cartViewModel: CartViewModel by activityViewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CartAdapter
    private lateinit var txtTotal: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_generate_sale, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerCart)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        txtTotal = view.findViewById(R.id.txtTotal)

        adapter = CartAdapter(
            onQuantityChange = { item, newCount ->
                cartViewModel.updateItemQuantity(item.productId, newCount)
            },
            onDelete = { item ->
                cartViewModel.removeItemFromCart(item.productId)
            }
        )

        recyclerView.adapter = adapter

        cartViewModel.cart.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products.toList())
            val total = products.sumOf { it.price.multiply(BigDecimal(it.count)) }
            txtTotal.text = "Total: s/. ${total}"
        }

        parentFragmentManager.setFragmentResultListener("barcode_result", viewLifecycleOwner) { _, bundle ->
            val barcode = bundle.getString("barcode") ?: return@setFragmentResultListener
            println("BARCODE: $barcode")
            cartViewModel.addProductByBarcode(barcode)
        }

        view.findViewById<Button>(R.id.btnScan).setOnClickListener {
            findNavController().navigate(R.id.action_generateSaleFragment_to_barcodeScannerFragment)
        }

        view.findViewById<Button>(R.id.btnSearch).setOnClickListener {
            findNavController().navigate(R.id.action_generateSaleFragment_to_selectProductsFragment)
        }
    }
}