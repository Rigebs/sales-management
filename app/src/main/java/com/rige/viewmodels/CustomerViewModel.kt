package com.rige.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rige.models.Customer
import com.rige.repositories.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel@Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _customers = MutableLiveData<List<Customer>>()
    val customers: LiveData<List<Customer>> get() = _customers

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun loadCustomers() {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                _customers.value = customerRepository.findAll()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}