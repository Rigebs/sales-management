package com.rige.viewmodels

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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SaleViewModel @Inject constructor(
    private val repository: SaleRepository,
    private val saleDetailRepository: SaleDetailRepository
) : ViewModel() {

    private val _pagedSales = MutableLiveData<List<SaleCustomer>>()
    val pagedSales: LiveData<List<SaleCustomer>> = _pagedSales

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isLastPage = MutableLiveData(false)
    val isLastPage: LiveData<Boolean> = _isLastPage

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentPage = 0
    private val pageSize = 10
    private var currentQuery = ""
    private var currentIsPaid: Boolean? = null

    fun resetAndLoad(query: String = "", isPaid: Boolean? = null) {
        currentPage = 0
        currentQuery = query
        currentIsPaid = isPaid
        _pagedSales.value = emptyList()
        _isLastPage.value = false
        loadNextPage()
    }

    val currentIsPaidFilter: Boolean?
        get() = currentIsPaid

    fun loadNextPageWithoutFilters() {
        if (_isLoading.value == true || _isLastPage.value == true) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val newPage = repository.findPaged(currentPage, pageSize)

                val currentList = _pagedSales.value.orEmpty()
                val updatedList = currentList + newPage
                _pagedSales.value = updatedList

                if (newPage.size < pageSize) {
                    _isLastPage.value = true
                } else {
                    currentPage++
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadNextPage() {
        if (_isLoading.value == true || _isLastPage.value == true) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val newPage = repository.findPagedWithFilters(
                    page = currentPage,
                    pageSize = pageSize,
                    searchQuery = currentQuery,
                    isPaid = currentIsPaid
                )

                val currentList = _pagedSales.value.orEmpty()
                val updatedList = currentList + newPage
                _pagedSales.value = updatedList

                if (newPage.size < pageSize) {
                    _isLastPage.value = true
                } else {
                    currentPage++
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleSaleStatus(sale: SaleCustomer) {
        viewModelScope.launch {
            try {
                val updatedSale = Sale(
                    id = sale.id,
                    date = sale.date,
                    total = sale.total,
                    customerId = sale.customerId,
                    isPaid = !sale.isPaid
                )
                repository.update(updatedSale)

                // Opcional: recargar con filtros actuales
                resetAndLoad(currentQuery, currentIsPaid)
            } catch (e: Exception) {
                _error.value = e.message
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

    private fun loadUntilPageSize() {
        if (_isLoading.value == true || _isLastPage.value == true) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                while (_pagedSales.value!!.size < pageSize && _isLastPage.value != true) {
                    val newPage = repository.findPagedWithFilters(
                        page = currentPage,
                        pageSize = pageSize,
                        searchQuery = currentQuery,
                        isPaid = currentIsPaid
                    )

                    val updatedList = _pagedSales.value.orEmpty() + newPage
                    _pagedSales.value = updatedList

                    if (newPage.size < pageSize) {
                        _isLastPage.value = true
                    } else {
                        currentPage++
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetAndLoadWithLocalFilter(query: String = "", isPaid: Boolean? = null) {
        currentPage = 0
        currentQuery = query
        currentIsPaid = isPaid
        _pagedSales.value = emptyList()
        _isLastPage.value = false

        val currentData = _pagedSales.value.orEmpty()
        val filtered = currentData.filter { sale ->
            (isPaid == null || sale.isPaid == isPaid) &&
                    (query.isBlank() || sale.customerName?.contains(query, ignoreCase = true) == true)
        }

        if (filtered.size >= pageSize) {
            _pagedSales.value = filtered.take(pageSize)
            _isLastPage.value = false
        } else {
            _pagedSales.value = filtered.toMutableList()
            loadUntilPageSize()
        }
    }
}