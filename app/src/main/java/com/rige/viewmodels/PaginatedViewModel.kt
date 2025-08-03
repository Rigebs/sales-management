package com.rige.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

abstract class PaginatedViewModel<T> : ViewModel() {
    private val _items = MutableLiveData<List<T>>(emptyList())
    val items: LiveData<List<T>> get() = _items

    protected var currentPage = 0
    protected val pageSize = 10
    protected var endReached = false
    val hasMore: Boolean get() = !endReached

    val isLoading = MutableLiveData(false)
    val error = MutableLiveData<String?>()

    abstract suspend fun fetchPage(page: Int, pageSize: Int): List<T>

    fun refresh() {
        currentPage = 0
        endReached = false
        _items.postValue(emptyList())
        loadNextPage()
    }

    fun loadNextPage() {
        if (isLoading.value == true || endReached) return

        isLoading.value = true
        viewModelScope.launch {
            try {
                val pageResult = fetchPage(currentPage, pageSize)
                val currentList = _items.value.orEmpty()

                val newItems = pageResult.filterNot { fetched ->
                    currentList.any { it == fetched }
                }

                if (newItems.isNotEmpty()) {
                    _items.postValue(currentList + newItems)
                    currentPage++
                }

                if (pageResult.size < pageSize) endReached = true
            } catch (e: Exception) {
                error.postValue(e.message)
            } finally {
                isLoading.postValue(false)
            }
        }
    }
}