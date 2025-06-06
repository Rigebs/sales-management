package com.rige.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rige.models.Barcode
import com.rige.repositories.BarcodeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import java.util.UUID

@HiltViewModel
class BarcodeViewModel @Inject constructor(
    private val barcodeRepository: BarcodeRepository
) : ViewModel() {

    private val _barcodes = MutableLiveData<List<Barcode>>(emptyList())
    val barcodes: LiveData<List<Barcode>> get() = _barcodes

    fun loadBarcodesByProduct(productId: String) {
        viewModelScope.launch {
            val result = barcodeRepository.findByProductId(productId)
            _barcodes.value = result
        }
    }

    fun addBarcodeLocally(barcode: String) {
        val currentList = _barcodes.value?.toMutableList() ?: mutableListOf()
        if (currentList.none { it.code == barcode }) {
            currentList.add(
                Barcode(
                    id = UUID.randomUUID().toString(),
                    code = barcode,
                    productId = ""
                )
            )
            _barcodes.value = currentList
        }
    }

    fun removeBarcodeLocally(barcode: String) {
        val currentList = _barcodes.value?.toMutableList() ?: mutableListOf()
        _barcodes.value = currentList.filterNot { it.code == barcode }
    }

    fun saveBarcode(code: String, productId: String) = viewModelScope.launch {
        try {
            val newBarcode = Barcode(
                id = UUID.randomUUID().toString(),
                code = code,
                productId = productId
            )
            barcodeRepository.save(newBarcode)
            loadBarcodesByProduct(productId)
        } catch (e: Exception) {
            Log.e("ProductVM", "Error guardando producto", e)
        }
    }

    fun saveAllBarcodes(productId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val toSave = _barcodes.value.orEmpty().map {
                it.copy(productId = productId)
            }
            barcodeRepository.saveAll(toSave)
            onComplete()
        }
    }

    fun deleteBarcode(barcodeId: String) = viewModelScope.launch {
        try {
            barcodeRepository.deleteById(barcodeId)
            val currentProductId = barcodes.value?.firstOrNull()?.productId
            currentProductId?.let {
                loadBarcodesByProduct(it)
            }
        } catch (e: Exception) {
            Log.e("ProductVM", "Error guardando producto", e)
        }
    }

    fun clearBarcodes() {
        _barcodes.value = emptyList()
    }
}