package com.rige.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rige.models.SaleCustomer
import com.rige.repositories.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SaleViewModel2 @Inject constructor(
    private val repository: SaleRepository
) : ViewModel() {

    private val _sales = MutableLiveData<List<SaleCustomer>>()
    val sales: LiveData<List<SaleCustomer>> = _sales

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val loadedSales = mutableListOf<SaleCustomer>()


    private var currentPage = 0
    private val pageSize = 10
    private var endReached = false
    val hasMore: Boolean get() = !endReached

    fun loadNextPage() {
        if (_isLoading.value == true || endReached) return

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val pageResult = repository.findPaged(currentPage, pageSize)

                // Filtrar duplicados por ID
                val newSales = pageResult.filterNot { sale ->
                    loadedSales.any { it.id == sale.id }
                }

                if (newSales.isNotEmpty()) {
                    loadedSales += newSales
                    _sales.value = loadedSales.toList()
                    currentPage++
                }

                if (pageResult.size < pageSize) {
                    endReached = true
                }

            } catch (e: Exception) {
                Log.e("SaleViewModel2", "Error al cargar ventas", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun reset() {
        currentPage = 0
        endReached = false
        loadedSales.clear()
        _sales.value = emptyList()
    }
}