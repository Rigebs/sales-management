package com.rige.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rige.models.OrderDetail
import com.rige.models.OrderSupplier
import com.rige.models.extra.OrderDetailView
import com.rige.models.extra.OrderFilterOptions
import com.rige.repositories.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _orders = MutableLiveData<List<OrderSupplier>>(emptyList())
    val orders: LiveData<List<OrderSupplier>> get() = _orders

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _orderDetails = MutableLiveData<List<OrderDetailView>>()
    val orderDetails: LiveData<List<OrderDetailView>> get() = _orderDetails

    // PAGINACIÓN
    private var currentPage = 0
    private val pageSize = 20
    private var endReached = false
    val hasMore: Boolean get() = !endReached

    private var currentFilters = OrderFilterOptions()

    fun refreshAndLoad(filters: OrderFilterOptions = OrderFilterOptions()) {
        currentPage = 0
        endReached = false
        currentFilters = filters
        _orders.postValue(emptyList()) // Reset de la lista actual
        loadNextPage()
    }

    fun loadNextPage() {
        if (_isLoading.value == true || endReached) return

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val pageResult = orderRepository.findPagedAdvancedOrders(
                    page = currentPage,
                    pageSize = pageSize,
                    filters = currentFilters
                )

                if (pageResult.isNotEmpty()) {
                    val currentList = _orders.value.orEmpty()

                    val newItems = pageResult.filterNot { fetched ->
                        currentList.any { it.id == fetched.id }
                    }

                    if (newItems.isNotEmpty()) {
                        _orders.postValue(currentList + newItems)
                        currentPage++
                    }

                    if (pageResult.size < pageSize) {
                        endReached = true
                    }
                } else {
                    endReached = true
                }

            } catch (e: Exception) {
                Log.e("OrderVM", "Error al cargar órdenes", e)
                _error.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun loadOrderDetails(orderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = orderRepository.findOrderDetailsByOrderId(orderId)
                _orderDetails.value = result
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun processOrder(
        supplierId: String?,
        total: BigDecimal,
        date: LocalDateTime,
        details: List<OrderDetail>
    ) {
        viewModelScope.launch {
            try {
                orderRepository.processOrder(
                    supplierId = supplierId,
                    total = total,
                    date = date,
                    details = details
                )
                refreshAndLoad(currentFilters) // Actualizar lista después de crear orden
            } catch (e: Exception) {
                _error.postValue(e.message)
            }
        }
    }
}