package com.rige

import com.rige.models.extra.FilterOptions

interface FilterCallback {
    fun onFiltersApplied(filters: FilterOptions)
}