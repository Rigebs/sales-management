package com.rige.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rige.models.SaleDetail
import com.rige.repositories.SaleDetailRepository
import kotlinx.coroutines.launch

class SaleDetailViewModel(private val repository: SaleDetailRepository) : ViewModel() {

    private val _details = MutableLiveData<List<SaleDetail>>()
    val details: LiveData<List<SaleDetail>> get() = _details

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun loadSaleDetails() {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                _details.value = repository.findAll()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}