package com.rige.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.rige.R
import com.rige.adapters.CartAdapter
import com.rige.adapters.CustomerAdapter
import com.rige.models.Customer
import com.rige.models.Sale
import com.rige.models.SaleDetail
import com.rige.viewmodels.CartViewModel
import com.rige.viewmodels.CustomerViewModel
import com.rige.viewmodels.SaleViewModel
import org.threeten.bp.LocalDateTime
import java.math.BigDecimal
import java.util.UUID

class GenerateSaleFragment : Fragment() {

    private val cartViewModel: CartViewModel by activityViewModels()
    private val saleViewModel: SaleViewModel by activityViewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CartAdapter
    private lateinit var txtTotal: TextView
    private lateinit var emptyView: View

    private var selectedCustomer: Customer? = null
    private val customerViewModel: CustomerViewModel by activityViewModels()

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
        emptyView = view.findViewById(R.id.emptyView)

        val btnAssignCustomer = view.findViewById<MaterialButton>(R.id.btnAssignCustomer)
        val paidSwitchContainer = view.findViewById<LinearLayout>(R.id.paidSwitchContainer)
        val btnClearCustomer = view.findViewById<ImageView>(R.id.btnClearCustomer)

        val btnSell = view.findViewById<Button>(R.id.btnSell)
        val switchPaid = view.findViewById<SwitchCompat>(R.id.switchPaid)

        btnSell.setOnClickListener {
            val cartItems = cartViewModel.cart.value ?: emptyList()

            if (cartItems.isEmpty()) {
                Toast.makeText(requireContext(), "No hay productos en el carrito.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val customer = selectedCustomer

            val isPaid = switchPaid.isChecked
            val saleId = UUID.randomUUID().toString()
            val now = LocalDateTime.now()

            val total = cartItems.sumOf { it.price.multiply(BigDecimal(it.count)) }

            val sale = Sale(
                id = saleId,
                date = now,
                isPaid = isPaid,
                total = total,
                customerId = customer?.id
            )

            val saleDetails = cartItems.map {
                SaleDetail(
                    id = UUID.randomUUID().toString(),
                    quantity = it.count,
                    unitPrice = it.price,
                    subtotal = it.price.multiply(BigDecimal(it.count)),
                    productId = it.productId,
                    saleId = saleId
                )
            }

            saleViewModel.saveSaleWithDetails(sale, saleDetails)

            cartViewModel.clearCart()
            selectedCustomer = null
            btnAssignCustomer.text = "ASIGNAR CLIENTE"
            paidSwitchContainer.visibility = View.GONE
            btnClearCustomer.visibility = View.GONE

            Toast.makeText(requireContext(), "Venta generada exitosamente.", Toast.LENGTH_SHORT).show()
        }

        btnAssignCustomer.setOnClickListener {
            showCustomerDialog { customer ->
                selectedCustomer = customer
                btnAssignCustomer.text = customer.name
                paidSwitchContainer.visibility = View.VISIBLE
                btnClearCustomer.visibility = View.VISIBLE
            }
        }

        btnClearCustomer.setOnClickListener {
            selectedCustomer = null
            btnAssignCustomer.text = "ASIGNAR CLIENTE"
            paidSwitchContainer.visibility = View.GONE
            btnClearCustomer.visibility = View.GONE
        }

        adapter = CartAdapter(
            onQuantityChange = { item, newCount ->
                if (newCount <= item.stock) {
                    cartViewModel.updateItemQuantity(item.productId, newCount)
                } else {
                    Toast.makeText(requireContext(), "Solo hay ${item.stock} unidades disponibles.", Toast.LENGTH_SHORT).show()
                }
            },
            onDelete = { item ->
                cartViewModel.removeItemFromCart(item.productId)
            }
        )

        recyclerView.adapter = adapter

        cartViewModel.cart.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products.toList())

            val total = products.sumOf { it.price.multiply(BigDecimal(it.count)) }
            txtTotal.text = "Total: s/. $total"

            if (products.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
            }
        }

        parentFragmentManager.setFragmentResultListener("barcode_result", viewLifecycleOwner) { _, bundle ->
            val barcode = bundle.getString("barcode") ?: return@setFragmentResultListener
            println("BARCODE: $barcode")
            cartViewModel.addProductByBarcode(requireContext(), barcode)
        }

        view.findViewById<Button>(R.id.btnScan).setOnClickListener {
            findNavController().navigate(R.id.action_generateSaleFragment_to_barcodeScannerFragment)
        }

        view.findViewById<Button>(R.id.btnSearch).setOnClickListener {
            findNavController().navigate(R.id.action_generateSaleFragment_to_selectProductsFragment)
        }
    }

    private fun showCustomerDialog(onCustomerSelected: (Customer) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_select_customer, null)
        val searchView = dialogView.findViewById<SearchView>(R.id.searchViewCustomers)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerCustomers)

        lateinit var customerDialog: AlertDialog

        val adapter = CustomerAdapter { customer ->
            onCustomerSelected(customer)
            customerDialog.dismiss()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        customerDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Seleccionar Cliente")
            .setNegativeButton("Cancelar", null)
            .create()

        customerViewModel.customers.observe(viewLifecycleOwner) { customerList ->
            adapter.submitList(customerList)
        }

        customerViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), "Error al cargar clientes: $it", Toast.LENGTH_SHORT).show()
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                val currentList = customerViewModel.customers.value ?: emptyList()
                adapter.submitList(currentList.filter {
                    it.name.contains(newText.orEmpty(), ignoreCase = true) ||
                            it.paternalSurname.contains(newText.orEmpty(), ignoreCase = true)
                })
                return true
            }
        })

        if (customerViewModel.customers.value.isNullOrEmpty()) {
            customerViewModel.loadCustomers()
        }

        customerDialog.show()
    }
}