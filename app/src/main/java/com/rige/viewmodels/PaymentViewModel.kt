package com.rige.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rige.models.Payment
import com.rige.repositories.PaymentRepository
import kotlinx.coroutines.launch

class PaymentViewModel(private val repository: PaymentRepository) : ViewModel() {

    private val _payments = MutableLiveData<List<Payment>>()
    val payments: LiveData<List<Payment>> get() = _payments

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun loadPayments() {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val result = repository.findAll()
                _payments.value = result
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePaymentById(id: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.deleteById(id)
                loadPayments()
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
}