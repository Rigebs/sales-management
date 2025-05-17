package com.rige.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.rige.databinding.FragmentProductFormBinding
import com.rige.models.Product
import com.rige.viewmodels.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import java.util.UUID
import com.rige.R

@AndroidEntryPoint
class ProductFormFragment : Fragment() {

    private lateinit var binding: FragmentProductFormBinding
    private val viewModel: ProductViewModel by activityViewModels()
    private var productId: String? = null
    private var currentProduct: Product? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        productId = arguments?.getString("productId")

        productId?.let { id ->
            viewModel.getProductById(id).observe(viewLifecycleOwner, Observer { product ->
                product?.let {
                    currentProduct = it
                    populateForm(it)
                }
            })
        }

        if (productId == null) {
            binding.cbStatus.isChecked = true
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val barCode = binding.etBarCode.text.toString().trim()
            val sellingPrice = binding.etSellingPrice.text.toString().toBigDecimalOrNull()
            val costPrice = binding.etCostPrice.text.toString().toBigDecimalOrNull()
            val quantity = binding.etQuantity.text.toString().toIntOrNull()
            val imageUrl = binding.etImageUrl.text.toString().trim()
            val status = binding.cbStatus.isChecked
            val categoryId = binding.etCategoryId.text.toString().trim().ifEmpty { null }

            if (name.isBlank() || sellingPrice == null || quantity == null) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val product = currentProduct?.copy(
                name = name,
                barCode = barCode,
                sellingPrice = sellingPrice,
                costPrice = costPrice,
                quantity = quantity,
                imageUrl = imageUrl,
                status = status,
                categoryId = categoryId,
                id = currentProduct?.id ?: UUID.randomUUID().toString()
            ) ?: Product(
                id = UUID.randomUUID().toString(),
                name = name,
                barCode = barCode,
                sellingPrice = sellingPrice,
                costPrice = costPrice,
                quantity = quantity,
                imageUrl = imageUrl,
                status = status,
                categoryId = categoryId
            )

            if (currentProduct == null) {
                viewModel.saveProduct(product)
            } else {
                viewModel.updateProduct(product)
            }
        }

        binding.barcodeLayout.setEndIconOnClickListener {
            findNavController().navigate(R.id.action_productFormFragment_to_barcodeScannerFragment)
        }

        parentFragmentManager.setFragmentResultListener("barcode_result", viewLifecycleOwner) { _, bundle ->
            val scannedBarcode = bundle.getString("barcode")
            scannedBarcode?.let {
                binding.etBarCode.setText(it)
            }
        }
    }

    private fun populateForm(product: Product) {
        binding.etName.setText(product.name)
        binding.etBarCode.setText(product.barCode)
        binding.etSellingPrice.setText(String.format(Locale.getDefault(), "%.2f", product.sellingPrice))
        binding.etCostPrice.setText(String.format(Locale.getDefault(), "%.2f", product.costPrice))
        binding.etQuantity.setText(product.quantity.toString())
        binding.etImageUrl.setText(product.imageUrl)
        binding.cbStatus.isChecked = product.status
        binding.etCategoryId.setText(product.categoryId)
    }
}