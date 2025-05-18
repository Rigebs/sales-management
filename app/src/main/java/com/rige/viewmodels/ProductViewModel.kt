package com.rige.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rige.models.Product
import com.rige.repositories.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun loadProducts() {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                _products.value = repository.findAll()
                println("PRODUCTOS: ${_products.value}")
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getProductById(id: String): LiveData<Product?> {
        val result = MutableLiveData<Product?>()
        viewModelScope.launch {
            try {
                result.value = repository.findById(id)
            } catch (e: Exception) {
                result.value = null
                _error.value = e.message
            }
        }
        return result
    }

    fun saveProduct(product: Product) = viewModelScope.launch {
        try {
            repository.save(product)
            loadProducts()
        } catch (e: Exception) {
            Log.e("ProductVM", "Error guardando producto", e)
        }
    }

    fun updateProduct(product: Product) = viewModelScope.launch {
        try {
            repository.update(product)
            loadProducts()
        } catch (e: Exception) {
            Log.e("ProductVM", "Error actualizando producto", e)
        }
    }
}