package com.rige.ui

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
import com.rige.adapters.PurchaseAdapter
import com.rige.databinding.FragmentGenerateOrderBinding
import com.rige.models.OrderDetail
import com.rige.utils.showQuantityDialog
import com.rige.viewmodels.OrderViewModel
import com.rige.viewmodels.PurchaseViewModel
import com.rige.viewmodels.SupplierViewModel
import org.threeten.bp.LocalDateTime
import java.util.UUID

class GenerateOrderFragment : Fragment() {

    private lateinit var binding: FragmentGenerateOrderBinding
    private val viewModel: PurchaseViewModel by activityViewModels()
    private val orderViewModel: OrderViewModel by activityViewModels()
    private val supplierViewModel: SupplierViewModel by activityViewModels()

    private lateinit var adapter: PurchaseAdapter
    private var currentSupplierId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGenerateOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentSupplierId = arguments?.getString("supplierId")

        setupRecyclerView()
        observeViewModel()

        currentSupplierId?.let { id ->
            supplierViewModel.loadSupplierById(id)
            observeSupplier()
        }

        parentFragmentManager.setFragmentResultListener("barcode_result", viewLifecycleOwner) { _, bundle ->
            val barcode = bundle.getString("barcode") ?: return@setFragmentResultListener
            println("BARCODE: $barcode")
            viewModel.addProductByBarcode(requireContext(), barcode)
        }

        binding.btnScanProduct.setOnClickListener {
            findNavController().navigate(R.id.action_generateOrderFragment_to_barcodeScannerFragment)
        }

        binding.btnSearchProduct.setOnClickListener {
            findNavController().navigate(R.id.action_generateOrderFragment_to_selectProductsPurchaseFragment)
        }

        binding.btnRegisterOrder.setOnClickListener {
            val supplierId = currentSupplierId
            val orderItems = viewModel.purchase.value.orEmpty()

            if (orderItems.isEmpty()) {
                Toast.makeText(requireContext(), "Faltan datos para registrar el pedido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val total = viewModel.getPurchaseTotal()
            val now = LocalDateTime.now()

            val orderId = UUID.randomUUID().toString()

            val orderDetails = orderItems.map {
                OrderDetail(
                    id = UUID.randomUUID().toString(),
                    productId = it.productId,
                    quantity = it.quantity,
                    unitPrice = it.unitPrice,
                    subtotal = it.subtotal,
                    orderId = orderId
                )
            }

            orderViewModel.processOrder(
                supplierId = supplierId,
                total = total,
                date = now,
                details = orderDetails
            )

            Toast.makeText(requireContext(), "Pedido registrado correctamente", Toast.LENGTH_SHORT).show()
            viewModel.clearPurchase()
        }
    }

    private fun setupRecyclerView() {
        adapter = PurchaseAdapter(
            onEdit = { item ->
                showQuantityDialog(requireContext(), item) { newQuantity ->
                    viewModel.updateItemQuantity(item.productId, newQuantity)
                }
            },
            onDelete = { item ->
                viewModel.removeItemFromPurchase(item.productId)
            }
        )
        binding.recyclerOrderItems.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerOrderItems.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.purchase.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)

            // Mostrar/ocultar vista vacía
            binding.emptyOrderView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE

            // Actualizar totales
            val totalItems = items.size
            binding.txtOrderTotalItems.text = "Total de productos: $totalItems"

            val totalCost = viewModel.getPurchaseTotal()
            binding.txtOrderTotalCost.text = "Total a gastar: s/. ${totalCost.setScale(2)}"
        }
    }

    private fun observeSupplier() {
        supplierViewModel.selectedSupplier.observe(viewLifecycleOwner) { supplier ->
            if (supplier != null) {
                binding.txtSupplier.text = "Proveedor: ${supplier.name}"
            } else {
                binding.txtSupplier.text = "Proveedor no encontrado"
            }
        }
    }
}