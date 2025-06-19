package com.rige.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rige.models.Sale
import com.rige.models.SaleCustomer
import com.rige.models.SaleDetail
import com.rige.repositories.SaleDetailRepository
import com.rige.repositories.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SaleViewModel @Inject constructor(
    private val repository: SaleRepository,
    private val saleDetailRepository: SaleDetailRepository
) : ViewModel() {

    private val _sales = MutableLiveData<List<SaleCustomer>>(emptyList())
    val sales: LiveData<List<SaleCustomer>> = _sales

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private var currentPage = 0
    private val pageSize = 10
    private var endReached = false
    val hasMore: Boolean get() = !endReached

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadNextPage(searchQuery: String? = null, isPaid: Boolean? = null) {
        if (_isLoading.value == true || endReached) return

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val pageResult = repository.findPaged(currentPage, pageSize, searchQuery, isPaid)

                if (pageResult.isNotEmpty()) {
                    val currentList = _sales.value.orEmpty()

                    // Evitar duplicados
                    val newItems = pageResult.filterNot { fetched ->
                        currentList.any { it.id == fetched.id }
                    }

                    if (newItems.isNotEmpty()) {
                        _sales.postValue(currentList + newItems)
                        currentPage++
                    }

                    if (pageResult.size < pageSize) {
                        endReached = true
                    }
                } else {
                    endReached = true
                }
            } catch (e: Exception) {
                Log.e("SaleViewModel2", "Error al cargar ventas", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun saveSaleWithDetails(sale: Sale, details: List<SaleDetail>) {
        viewModelScope.launch {
            try {
                repository.save(sale)
                saleDetailRepository.saveAll(details)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun refreshAndLoad(searchQuery: String?, isPaid: Boolean?) {
        currentPage = 0
        endReached = false
        _sales.value = emptyList()
        loadNextPage(searchQuery, isPaid)
    }

    fun reset() {
        currentPage = 0
        endReached = false
        _sales.value = emptyList()
    }

    fun ensureInitialDataLoaded() {
        if (_sales.value.isNullOrEmpty()) {
            loadNextPage()
        }
    }
}