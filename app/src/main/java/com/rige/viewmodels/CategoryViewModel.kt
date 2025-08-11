package com.rige.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rige.models.Category
import com.rige.repositories.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CategoryRepository
): ViewModel() {

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> get() = _categories

    private val _selectedCategory = MutableLiveData<Category?>()
    val selectedCategory: LiveData<Category?> get() = _selectedCategory

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun loadCategories() {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                _categories.value = repository.findAll()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCategoryById(id: String) {
        viewModelScope.launch {
            try {
                _selectedCategory.value = repository.findById(id)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun saveCategory(category: Category) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                repository.save(category)
                loadCategories()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCategory(category: Category) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                repository.update(category)
                loadCategories()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSelectedCategory() {
        _selectedCategory.value = null
    }
}