package com.rige.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rige.models.Product
import com.rige.models.extra.ProductFilterOptions
import com.rige.repositories.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _products = MutableLiveData<List<Product>>(emptyList())
    val products: LiveData<List<Product>> get() = _products

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentPage = 0
    private val pageSize = 10
    private var endReached = false
    val hasMore: Boolean get() = !endReached

    private var currentFilters = ProductFilterOptions() // filtros actuales

    fun refreshAndLoad(filters: ProductFilterOptions = ProductFilterOptions()) {
        currentPage = 0
        endReached = false
        currentFilters = filters
        _products.postValue(emptyList()) // reset de la lista actual
        loadNextPage()
    }

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

    fun loadNextPage() {
        if (_isLoading.value == true || endReached) return

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val pageResult = repository.findPagedProducts(
                    page = currentPage,
                    pageSize = pageSize,
                    filters = currentFilters
                )

                if (pageResult.isNotEmpty()) {
                    val currentList = _products.value.orEmpty()

                    val newItems = pageResult.filterNot { fetched ->
                        currentList.any { it.id == fetched.id }
                    }

                    if (newItems.isNotEmpty()) {
                        _products.postValue(currentList + newItems)
                        currentPage++
                    }

                    if (pageResult.size < pageSize) {
                        endReached = true
                    }
                } else {
                    endReached = true
                }

            } catch (e: Exception) {
                Log.e("ProductVM", "Error al cargar productos", e)
                _error.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
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

    fun saveProduct(product: Product, onComplete: () -> Unit = {}) = viewModelScope.launch {
        try {
            repository.save(product)
            refreshAndLoad(currentFilters) // actualizar la lista paginada
            onComplete()
        } catch (e: Exception) {
            Log.e("ProductVM", "Error guardando producto", e)
            _error.value = e.message
        }
    }

    fun updateProduct(product: Product, onComplete: (() -> Unit)? = null) = viewModelScope.launch {
        try {
            repository.update(product)
            refreshAndLoad(currentFilters) // actualizar la lista paginada
            onComplete?.invoke()
        } catch (e: Exception) {
            Log.e("ProductVM", "Error actualizando producto", e)
            _error.value = e.message
        }
    }

    fun toggleStatus(product: Product) = viewModelScope.launch {
        try {
            val updatedProduct = product.copy(status = !product.status)
            repository.update(updatedProduct)
            refreshAndLoad(currentFilters) // actualizar la lista paginada
        } catch (e: Exception) {
            Log.e("ProductVM", "Error cambiando estado del producto", e)
            _error.value = "No se pudo cambiar el estado del producto"
        }
    }
}
