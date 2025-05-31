package com.rige.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rige.models.Sale
import com.rige.models.SaleDetail
import com.rige.repositories.SaleDetailRepository
import com.rige.repositories.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SaleViewModel @Inject constructor(
    private val repository: SaleRepository,
    private val saleDetailViewModel: SaleDetailRepository
) : ViewModel() {

    private val _sales = MutableLiveData<List<Sale>>()
    val sales: LiveData<List<Sale>> get() = _sales

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

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

    fun saveSaleWithDetails(sale: Sale, details: List<SaleDetail>) {
        viewModelScope.launch {
            try {
                repository.save(sale)
                saleDetailViewModel.saveAll(details)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}