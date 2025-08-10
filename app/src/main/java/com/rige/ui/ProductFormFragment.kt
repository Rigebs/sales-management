package com.rige.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.rige.R
import com.rige.databinding.FragmentProductFormBinding
import com.rige.models.Product
import com.rige.viewmodels.ProductViewModel
import java.util.UUID
import com.rige.clients.CloudinaryClient
import com.rige.models.Barcode
import com.rige.models.Category
import com.rige.utils.formatDecimal
import com.rige.viewmodels.BarcodeViewModel
import com.rige.viewmodels.CategoryViewModel
import java.io.File
import java.math.BigDecimal

class ProductFormFragment : Fragment() {

    private lateinit var binding: FragmentProductFormBinding
    private val viewModel: ProductViewModel by activityViewModels()
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private val barcodeViewModel: BarcodeViewModel by activityViewModels()

    private var productId: String? = null
    private var currentProduct: Product? = null
    private var formInitialized = false
    private var barcodesInitialized = false
    private var selectedImageUrl: String? = null
    private var imageUriPreview: Uri? = null
    private var cameraImageUri: Uri? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProductFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        productId = arguments?.getString("productId")
        setupImagePickerLauncher()
        setupObservers()
        setupListeners()
        categoryViewModel.loadCategories()
        setupMeasureUnitSpinner()

        if (productId == null) {
            barcodeViewModel.clearBarcodes()
            if (!barcodesInitialized) {
                if (barcodeViewModel.barcodes.value.isNullOrEmpty()) {
                    barcodeViewModel.clearBarcodes()
                }
                barcodesInitialized = true
            }
        } else {
            if (!barcodesInitialized) {
                barcodeViewModel.loadBarcodesByProduct(productId!!)

                barcodesInitialized = true
            }

            if (currentProduct == null) {
                loadProduct(productId!!)
            }
        }

    }

    private fun setupObservers() {
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            setupCategorySpinner(categories)
        }

        barcodeViewModel.barcodes.observe(viewLifecycleOwner) { barcodes ->
            if (barcodes.isNotEmpty()) {
                displayBarcodeChips(barcodes)
            }
        }

        parentFragmentManager.setFragmentResultListener("barcode_result", viewLifecycleOwner) { _, bundle ->
            bundle.getString("barcode")?.let { code ->
                val currentCodes = barcodeViewModel.barcodes.value.orEmpty()
                if (currentCodes.none { it.code == code }) {
                    if (productId != null) barcodeViewModel.saveBarcode(code, productId!!)
                    else barcodeViewModel.addBarcodeLocally(code)
                } else Toast.makeText(requireContext(), "Código ya escaneado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() = with(binding) {
        btnSave.setOnClickListener { handleSaveClick() }

        btnSelectImage.setOnClickListener { launchImagePicker() }

        btnCancelImage.setOnClickListener {
            imageUriPreview = null
            selectedImageUrl = null
            ivPreview.setImageDrawable(null)
            ivPreview.visibility = View.GONE
            imageActionButtons.visibility = View.GONE
            btnSelectImage.visibility = View.VISIBLE
        }

        btnConfirmImage.setOnClickListener {
            imageUriPreview?.let { uri ->
                CloudinaryClient.uploadImage(requireContext(), uri) { imageUrl ->
                    requireActivity().runOnUiThread {
                        if (imageUrl != null) {
                            selectedImageUrl = imageUrl
                            Toast.makeText(requireContext(), "Imagen subida", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Error al subir imagen", Toast.LENGTH_LONG).show()
                        }
                        imageActionButtons.visibility = View.GONE
                        btnSelectImage.visibility = View.VISIBLE
                    }
                }
            }
        }

        btnScanBarcode.setOnClickListener {
            findNavController().navigate(R.id.action_productFormFragment_to_barcodeScannerFragment)
        }

        cbManageStock.setOnCheckedChangeListener { _, isChecked ->
            binding.tilQuantity.visibility = if (isChecked) View.VISIBLE else View.GONE
            binding.tilCostPrice.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        cbIsDecimal.setOnCheckedChangeListener { _, isChecked ->
            binding.tilMeasureUnit.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (isChecked) {
                binding.etQuantity.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            } else {
                binding.etQuantity.inputType = InputType.TYPE_CLASS_NUMBER
            }
        }
    }

    private fun setupMeasureUnitSpinner() {
        val units = listOf("KG", "LT")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, units)
        binding.spinnerMeasureUnit.setAdapter(adapter)
    }

    private fun setupImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                imageUriPreview = result.data?.data ?: cameraImageUri
                imageUriPreview?.let {
                    binding.ivPreview.setImageURI(it)
                    binding.ivPreview.visibility = View.VISIBLE
                    binding.imageActionButtons.visibility = View.VISIBLE
                    binding.btnSelectImage.visibility = View.GONE
                }
            }
        }
    }

    private fun launchImagePicker() {
        val galleryIntent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        val imageFile = File.createTempFile("product_", ".jpg", requireContext().cacheDir)
        cameraImageUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", imageFile)
        val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(android.provider.MediaStore.EXTRA_OUTPUT, cameraImageUri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(galleryIntent, "Seleccionar imagen o tomar foto").apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
        }
        imagePickerLauncher.launch(chooser)
    }

    private fun handleSaveClick() {
        val name = binding.etName.text.toString().trim()
        val sellingPrice = binding.etSellingPrice.text.toString().toBigDecimalOrNull()
        val imageUrl = selectedImageUrl ?: currentProduct?.imageUrl.orEmpty()
        val status = binding.cbStatus.isChecked

        val selectedCategoryName = binding.spinnerCategory.text.toString()
        val categoryId = categoryViewModel.categories.value
            ?.find { it.name == selectedCategoryName }
            ?.id?.takeIf { it.isNotEmpty() }

        val manageStock = binding.cbManageStock.isChecked
        val isDecimal = binding.cbIsDecimal.isChecked
        val measureUnit = if (isDecimal) binding.spinnerMeasureUnit.text.toString().takeIf { it.isNotBlank() } else null

        val quantity = if (manageStock) binding.etQuantity.text.toString().toBigDecimalOrNull() ?: BigDecimal.ZERO else BigDecimal.ZERO
        val costPrice = if (manageStock) binding.etCostPrice.text.toString().toBigDecimalOrNull() else null


        if (name.isBlank() || sellingPrice == null || quantity == null) {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val newProduct = (currentProduct?.copy(
            name = name,
            sellingPrice = sellingPrice,
            costPrice = costPrice,
            quantity = quantity,
            imageUrl = imageUrl,
            status = status,
            categoryId = categoryId,
            manageStock = manageStock,
            isDecimal = isDecimal,
            measureUnit = measureUnit
        ) ?: Product(
            id = UUID.randomUUID().toString(),
            name = name,
            isDecimal = isDecimal,
            measureUnit = measureUnit,
            sellingPrice = sellingPrice,
            costPrice = costPrice,
            quantity = quantity,
            imageUrl = imageUrl,
            status = status,
            categoryId = categoryId,
            manageStock = manageStock
        ))

        if (currentProduct == newProduct) {
            findNavController().popBackStack()
            return
        }

        if (currentProduct == null) {
            viewModel.saveProduct(newProduct) {
                barcodeViewModel.saveAllBarcodes(newProduct.id) {
                    findNavController().popBackStack()
                }
            }
        } else {
            viewModel.updateProduct(newProduct) {
                findNavController().popBackStack()
            }
        }
    }

    private fun loadProduct(id: String) {
        viewModel.getProductById(id).observe(viewLifecycleOwner) { product ->
            product?.let {
                currentProduct = it
                if (!formInitialized) {
                    populateForm(it)
                    formInitialized = true
                }
            }
        }
    }

    private fun populateForm(product: Product) = with(binding) {
        // Nombre
        etName.setText(product.name)

        // Precios
        etSellingPrice.setText(product.sellingPrice.formatDecimal())
        etCostPrice.setText(product.costPrice?.formatDecimal())

        // Cantidad
        etQuantity.setText(product.quantity.toString())

        // Estado
        cbStatus.isChecked = product.status

        // Imagen
        if (!product.imageUrl.isNullOrBlank()) {
            Glide.with(this@ProductFormFragment)
                .load(product.imageUrl)
                .into(ivPreview)
            ivPreview.visibility = View.VISIBLE
        }

        // Categoría
        categoryViewModel.categories.value?.let { categories ->
            val selectedCategory = categories.find { it.id == product.categoryId }
            spinnerCategory.setText(selectedCategory?.name ?: "Sin categoría", false)
        }

        // === Inventario ===
        cbManageStock.isChecked = product.manageStock
        tilQuantity.visibility = if (product.manageStock) View.VISIBLE else View.GONE
        tilCostPrice.visibility = if (product.manageStock) View.VISIBLE else View.GONE

        // === Producto a granel ===
        cbIsDecimal.isChecked = product.isDecimal
        tilMeasureUnit.visibility = if (product.isDecimal) View.VISIBLE else View.GONE

        if (product.isDecimal) {
            spinnerMeasureUnit.setText(product.measureUnit ?: "", false)
            etQuantity.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        } else {
            etQuantity.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }

    private fun setupCategorySpinner(categories: List<Category>) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            listOf("Sin categoría") + categories.map { it.name }
        )
        binding.spinnerCategory.setAdapter(adapter)
    }

    private fun displayBarcodeChips(barcodes: List<Barcode>) {
        with(binding.barcodeListContainer) {
            removeAllViews()
            visibility = if (barcodes.isEmpty()) View.GONE else View.VISIBLE
            barcodes.forEach { barcode ->
                val chip = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_barcode_chip, this, false) as Chip
                chip.text = barcode.code
                chip.setOnCloseIconClickListener {
                    barcodeViewModel.removeBarcodeLocally(barcode.code)
                    if (productId != null) barcodeViewModel.deleteBarcode(barcode.id)
                }
                addView(chip)
            }
        }
    }
}
