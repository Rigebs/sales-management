package com.rige.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rige.models.CartItem
import com.rige.models.Sale
import com.rige.repositories.ProductRepository
import com.rige.repositories.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class SaleViewModel@Inject constructor(
    private val repository: SaleRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _sales = MutableLiveData<List<Sale>>()
    val sales: LiveData<List<Sale>> get() = _sales

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _cart = MutableLiveData<List<CartItem>>(emptyList())
    val cart: LiveData<List<CartItem>> get() = _cart

    fun loadSales() {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                _sales.value = repository.findAll()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

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

    fun addProductByBarcode(barcode: String) {
        viewModelScope.launch {
            val product = productRepository.findByBarcode(barcode)
            println("PRODUCT: $product")
            product?.let {
                addItemToCart(it.id, it.name, it.imageUrl.toString(), it.sellingPrice)
            }
        }
    }
}