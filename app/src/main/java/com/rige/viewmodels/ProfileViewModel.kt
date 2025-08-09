package com.rige.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rige.repositories.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _hasAccess = MutableStateFlow<Boolean?>(null)
    val hasAccess: StateFlow<Boolean?> = _hasAccess

    fun validateAccess(userId: String) {
        viewModelScope.launch {
            val access = repository.validateCurrentUserAccess(userId)
            _hasAccess.value = access
        }
    }
}