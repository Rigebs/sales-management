package com.rige.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import java.util.UUID
import com.rige.clients.CloudinaryClient
import com.rige.models.Category
import com.rige.viewmodels.BarcodeViewModel
import com.rige.viewmodels.CategoryViewModel
import java.io.File

@AndroidEntryPoint
class ProductFormFragment : Fragment() {

    private lateinit var binding: FragmentProductFormBinding
    private val viewModel: ProductViewModel by activityViewModels()
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private val barcodeViewModel: BarcodeViewModel by activityViewModels()

    private var productId: String? = null
    private var currentProduct: Product? = null

    private var formInitialized = false

    private var selectedImageUrl: String? = null

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private var imageUriPreview: Uri? = null

    private var cameraImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        productId = arguments?.getString("productId")

        productId?.let { id ->
            viewModel.getProductById(id).observe(viewLifecycleOwner) { product ->
                product?.let {
                    currentProduct = it
                    if (!formInitialized) {
                        populateForm(it)
                        formInitialized = true
                        barcodeViewModel.clearBarcodes()
                        barcodeViewModel.loadBarcodesByProduct(id)
                    }
                }
            }
        }

        if (productId == null) {
            binding.cbStatus.isChecked = true
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val sellingPrice = binding.etSellingPrice.text.toString().toBigDecimalOrNull()
            val costPrice = binding.etCostPrice.text.toString().toBigDecimalOrNull()
            val quantity = binding.etQuantity.text.toString().toIntOrNull()
            val imageUrl = selectedImageUrl ?: currentProduct?.imageUrl.orEmpty()
            val status = binding.cbStatus.isChecked

            val selectedCategoryName = binding.spinnerCategory.text.toString()
            val categoriesWithNone = listOf(Category(id = "", name = "Sin categoría")) + (categoryViewModel.categories.value ?: emptyList())
            val selectedCategory = categoriesWithNone.find { it.name == selectedCategoryName }
            val categoryId = selectedCategory?.id?.takeIf { it.isNotEmpty() }

            if (name.isBlank() || sellingPrice == null || quantity == null) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val product = currentProduct?.copy(
                name = name,
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
                sellingPrice = sellingPrice,
                costPrice = costPrice,
                quantity = quantity,
                imageUrl = imageUrl,
                status = status,
                categoryId = categoryId
            )

            val productIdToUse = product.id

            val action = {
                barcodeViewModel.saveAllBarcodes(productIdToUse) {
                    findNavController().popBackStack()
                }
            }

            if (currentProduct == null) {
                viewModel.saveProduct(product, onComplete = action)
            } else {
                viewModel.updateProduct(product, onComplete = action)
            }
        }

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val dataUri = result.data?.data
                val isFromCamera = dataUri == null && cameraImageUri != null

                imageUriPreview = if (isFromCamera) cameraImageUri else dataUri

                imageUriPreview?.let {
                    binding.ivPreview.setImageURI(it)

                    binding.ivPreview.visibility = View.VISIBLE
                    binding.imageActionButtons.visibility = View.VISIBLE
                    binding.btnSelectImage.visibility = View.GONE
                }
            }
        }

        binding.btnSelectImage.setOnClickListener {
            val context = requireContext()

            val galleryIntent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }

            val imageFile = File.createTempFile("product_", ".jpg", context.cacheDir)
            cameraImageUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)

            val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(android.provider.MediaStore.EXTRA_OUTPUT, cameraImageUri)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(galleryIntent, "Seleccionar imagen o tomar foto")
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

            imagePickerLauncher.launch(chooser)
        }

        binding.btnCancelImage.setOnClickListener {
            imageUriPreview = null
            selectedImageUrl = null
            binding.ivPreview.setImageDrawable(null)
            binding.ivPreview.visibility = View.GONE
            binding.imageActionButtons.visibility = View.GONE
            binding.btnSelectImage.visibility = View.VISIBLE
        }

        binding.btnConfirmImage.setOnClickListener {
            imageUriPreview?.let { uri ->
                CloudinaryClient.uploadImage(requireContext(), uri) { imageUrl ->
                    requireActivity().runOnUiThread {
                        if (imageUrl != null) {
                            selectedImageUrl = imageUrl
                            Toast.makeText(requireContext(), "Imagen subida", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Error al subir imagen", Toast.LENGTH_LONG).show()
                        }

                        binding.imageActionButtons.visibility = View.GONE
                        binding.btnSelectImage.visibility = View.VISIBLE
                    }
                }
            }
        }

        binding.btnScanBarcode.setOnClickListener {
            findNavController().navigate(R.id.action_productFormFragment_to_barcodeScannerFragment)
        }

        parentFragmentManager.setFragmentResultListener("barcode_result", viewLifecycleOwner) { _, bundle ->
            val scannedBarcode = bundle.getString("barcode")
            scannedBarcode?.let {
                val currentCodes = barcodeViewModel.barcodes.value.orEmpty()
                if (currentCodes.none { b -> b.code == it }) {
                    barcodeViewModel.addBarcodeLocally(it)
                } else {
                    Toast.makeText(requireContext(), "Código ya escaneado", Toast.LENGTH_SHORT).show()
                }
            }
        }

        categoryViewModel.loadCategories()
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            setupCategorySpinner(categories)

            currentProduct?.let { product ->
                val categoriesWithNone = listOf(Category(id = "", name = "Sin categoría")) + categories
                val selectedCategory = categoriesWithNone.find { it.id == product.categoryId } ?: categoriesWithNone[0]
                binding.spinnerCategory.setText(selectedCategory.name, false)
            }
        }

        barcodeViewModel.barcodes.observe(viewLifecycleOwner) { barcodes ->
            binding.barcodeListContainer.removeAllViews()
            barcodes.forEach { barcode ->
                val chip = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_barcode_chip, binding.barcodeListContainer, false) as Chip
                chip.text = barcode.code
                chip.setOnCloseIconClickListener {
                    barcodeViewModel.removeBarcodeLocally(barcode.code)
                }
                binding.barcodeListContainer.addView(chip)
            }
        }

    }

    override fun onResume() {
        super.onResume()

        if (imageUriPreview == null && currentProduct?.imageUrl?.isNotBlank() == true) {
            binding.ivPreview.visibility = View.VISIBLE
            Glide.with(this)
                .load(currentProduct!!.imageUrl)
                .into(binding.ivPreview)
        }
    }

    private fun populateForm(product: Product) {
        binding.etName.setText(product.name)
        binding.etSellingPrice.setText(String.format(Locale.getDefault(), "%.2f", product.sellingPrice))
        binding.etCostPrice.setText(String.format(Locale.getDefault(), "%.2f", product.costPrice))
        binding.etQuantity.setText(product.quantity.toString())
        if (!product.imageUrl.isNullOrBlank()) {
            binding.ivPreview.visibility = View.VISIBLE
            Glide.with(this)
                .load(product.imageUrl)
                .into(binding.ivPreview)

            binding.btnSelectImage.visibility = View.VISIBLE
        }
        binding.cbStatus.isChecked = product.status
        val categories = categoryViewModel.categories.value
        categories?.let {
            val categoriesWithNone = listOf(Category(id = "", name = "Sin categoría")) + it
            val selectedCategory = categoriesWithNone.find { c -> c.id == product.categoryId } ?: categoriesWithNone[0]
            binding.spinnerCategory.setText(selectedCategory.name, false)
        }
    }

    private fun setupCategorySpinner(categories: List<Category>) {
        val categoriesWithNone = listOf(Category(id = "", name = "Sin categoría")) + categories
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            categoriesWithNone.map { it.name }
        )
        binding.spinnerCategory.setAdapter(adapter)
    }
}