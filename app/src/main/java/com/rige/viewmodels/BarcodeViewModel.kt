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
        println("BarcodeViewModel: addBarcodeLocally called with barcode: $barcode")
        val currentList = _barcodes.value?.toMutableList() ?: mutableListOf()
        println("Current list: ${_barcodes.value}")
        if (currentList.none { it.code == barcode }) {
            currentList.add(
                Barcode(
                    id = UUID.randomUUID().toString(),
                    code = barcode,
                    productId = ""
                )
            )
            _barcodes.value = currentList
            println("List size: ${_barcodes.value}")
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

            val currentList = _barcodes.value.orEmpty().toMutableList()
            currentList.add(newBarcode)
            _barcodes.value = currentList
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
        } catch (e: Exception) {
            Log.e("ProductVM", "Error guardando producto", e)
        }
    }

    fun clearBarcodes() {
        _barcodes.value = emptyList()
    }
}