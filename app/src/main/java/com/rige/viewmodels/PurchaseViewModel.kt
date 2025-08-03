package com.rige.viewmodels

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rige.models.extra.PurchaseItem
import com.rige.repositories.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class PurchaseViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _purchase = MutableLiveData<List<PurchaseItem>>(emptyList())
    val purchase: LiveData<List<PurchaseItem>> get() = _purchase

    fun addItemToPurchase(
        productId: String,
        name: String,
        isDecimal: Boolean,
        measureUnit: String?,
        stock: BigDecimal,
        imageUrl: String,
        unitPrice: BigDecimal,
        quantityToAdd: BigDecimal = BigDecimal.ONE
    ) {
        val current = _purchase.value?.toMutableList() ?: mutableListOf()
        val index = current.indexOfFirst { it.productId == productId }

        if (index != -1) {
            val existing = current[index]
            val newQuantity = existing.quantity + quantityToAdd
            current[index] = existing.copy(quantity = newQuantity)
        } else {
            current.add(
                PurchaseItem(
                    productId = productId,
                    name = name,
                    isDecimal = isDecimal,
                    measureUnit = measureUnit,
                    stock = stock,
                    imageUrl = imageUrl,
                    unitPrice = unitPrice,
                    quantity = quantityToAdd
                )
            )
        }
        _purchase.value = current
    }

    fun updateItemQuantity(productId: String, newQuantity: BigDecimal) {
        val current = _purchase.value?.toMutableList() ?: return
        val index = current.indexOfFirst { it.productId == productId }
        if (index != -1) {
            current[index] = current[index].copy(quantity = newQuantity)
            _purchase.value = current
        }
    }

    fun removeItemFromPurchase(productId: String) {
        val current = _purchase.value?.toMutableList() ?: return
        _purchase.value = current.filter { it.productId != productId }
    }

    fun clearPurchase() {
        _purchase.value = emptyList()
    }

    fun addProductByBarcode(context: Context, barcode: String) {
        viewModelScope.launch {
            val product = productRepository.findByBarcode(barcode)
            if (product != null) {
                val currentPurchase = _purchase.value.orEmpty()
                val existingItem = currentPurchase.find { it.productId == product.id }

                if (existingItem != null) {
                    val newQuantity = existingItem.quantity + BigDecimal.ONE
                    if (newQuantity <= product.quantity) {
                        updateItemQuantity(product.id, newQuantity)
                    } else {
                        Toast.makeText(
                            context,
                            "Stock máximo alcanzado para '${product.name}'.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    if (product.quantity > BigDecimal.ZERO) {
                        addItemToPurchase(
                            product.id,
                            product.name,
                            product.isDecimal,
                            product.measureUnit,
                            product.quantity,
                            product.imageUrl.toString(),
                            product.sellingPrice
                        )
                    } else {
                        Toast.makeText(
                            context,
                            "Producto sin stock disponible.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    context,
                    "Producto no encontrado con ese código",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun getPurchaseTotal(): BigDecimal {
        return _purchase.value.orEmpty().fold(BigDecimal.ZERO) { acc, item ->
            acc + item.subtotal
        }
    }
}