package com.rige.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rige.models.Sale
import com.rige.models.SaleCustomer
import com.rige.models.SaleDetail
import com.rige.models.extra.FilterOptions
import com.rige.models.extra.SaleDetailView
import com.rige.models.extra.SaleWithDetails
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

    private val _saleWithDetails = MutableLiveData<List<SaleDetailView>>(emptyList())
    val saleWithDetails: LiveData<List<SaleDetailView>> = _saleWithDetails

    private var currentPage = 0
    private val pageSize = 10
    private var endReached = false
    val hasMore: Boolean get() = !endReached

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentFilters = FilterOptions()

    fun refreshAndLoad(filters: FilterOptions = FilterOptions()) {
        currentPage = 0
        endReached = false
        currentFilters = filters
        _sales.postValue(emptyList()) // <- ESTA LÍNEA ES LA CLAVE
        loadNextPage()
    }

    fun loadNextPage() {
        if (_isLoading.value == true || endReached) return

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val pageResult = repository.findPagedAdvanced(
                    currentPage,
                    pageSize,
                    currentFilters
                )

                if (pageResult.isNotEmpty()) {
                    val currentList = _sales.value.orEmpty()

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
                Log.e("SaleViewModel", "Error al cargar ventas", e)
                _error.postValue(e.message)
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

    fun getSaleWithDetailsById(id: String) {
        viewModelScope.launch {
            try {
                val saleDetails = repository.findSaleWithDetailsById(id)
                _saleWithDetails.postValue(saleDetails)
            } catch (e: Exception) {
                Log.e("SaleViewModel", "Error al obtener detalles de venta", e)
                _error.postValue(e.message)
            }
        }
    }

    fun ensureInitialDataLoaded() {
        if (_sales.value.isNullOrEmpty()) {
            loadNextPage()
        }
    }
}