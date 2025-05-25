package com.rige.viewmodels

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rige.models.CartItem
import com.rige.repositories.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _cart = MutableLiveData<List<CartItem>>(emptyList())
    val cart: LiveData<List<CartItem>> get() = _cart

    fun addItemToCart(productId: String, name: String, imageUrl: String, price: BigDecimal) {
        val current = _cart.value?.toMutableList() ?: mutableListOf()
        val index = current.indexOfFirst { it.productId == productId }
        if (index != -1) {
            val updatedItem = current[index].copy(count = current[index].count + 1)
            current[index] = updatedItem
        } else {
            current.add(CartItem(productId, name, imageUrl, price, count = 1))
        }
        _cart.value = current
    }

    fun updateItemQuantity(productId: String, newCount: Int) {
        val current = _cart.value?.toMutableList() ?: return
        val index = current.indexOfFirst { it.productId == productId }
        if (index != -1) {
            current[index] = current[index].copy(count = newCount)
            _cart.value = current
        }
    }

    fun removeItemFromCart(productId: String) {
        val current = _cart.value?.toMutableList() ?: return
        _cart.value = current.filter { it.productId != productId }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun addProductByBarcode(context: Context, barcode: String) {
        viewModelScope.launch {
            val product = productRepository.findByBarcode(barcode)
            if (product != null) {
                addItemToCart(product.id, product.name, product.imageUrl.toString(), product.sellingPrice)
            } else {
                Toast.makeText(context, "Producto no encontrado con ese código", Toast.LENGTH_SHORT).show()
            }
        }
    }
}