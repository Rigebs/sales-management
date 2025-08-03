package com.rige.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rige.models.Product
import com.rige.models.Supplier
import com.rige.repositories.SupplierRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupplierViewModel @Inject constructor(
    private val repository: SupplierRepository
) : ViewModel() {

    private val _suppliers = MutableLiveData<List<Supplier>>(emptyList())
    val suppliers: LiveData<List<Supplier>> get() = _suppliers

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _selectedSupplier = MutableLiveData<Supplier?>()
    val selectedSupplier: LiveData<Supplier?> get() = _selectedSupplier

    fun loadSuppliers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val list = repository.findAll()
                _suppliers.value = list
            } catch (e: Exception) {
                _error.value = "Error al cargar proveedores: ${e.message}"

                println("Error al cargar proveedores: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveSupplier(supplier: Supplier) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.save(supplier)
                loadSuppliers() // Recarga la lista después de guardar
            } catch (e: Exception) {
                _error.value = "Error al guardar proveedor: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadSupplierById(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val supplier = repository.findById(id)
                _selectedSupplier.value = supplier
            } catch (e: Exception) {
                _error.value = "Error al buscar proveedor: ${e.message}"
                println("Error al buscar proveedor por ID: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}