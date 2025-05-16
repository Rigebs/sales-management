package com.rige.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rige.models.PaymentMethod
import com.rige.repositories.PaymentMethodRepository
import kotlinx.coroutines.launch

class PaymentMethodViewModel(private val repository: PaymentMethodRepository) : ViewModel() {

    private val _methods = MutableLiveData<List<PaymentMethod>>()
    val methods: LiveData<List<PaymentMethod>> get() = _methods

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun loadPaymentMethods() {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                _methods.value = repository.findAll()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}