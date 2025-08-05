package com.rige.utils

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import com.rige.R

// ChipUtils.kt
fun createStatusChip(context: Context, status: String, isChecked: Boolean = false): Chip {
    return Chip(context).apply {
        text = status
        isCheckable = true
        isClickable = true
        id = View.generateViewId()
        this.isChecked = isChecked
        chipIcon = ContextCompat.getDrawable(context, when (status) {
            "Activos" -> R.drawable.ic_green_circle
            "Inactivos" -> R.drawable.ic_red_circle
            else -> R.drawable.ic_gray_circle
        })
        isChipIconVisible = true
        chipIconSize = 24f
        chipIconTint = null
    }
}

fun createCategoryChip(context: Context, name: String, isChecked: Boolean = false): Chip {
    return Chip(context).apply {
        text = name
        isCheckable = true
        isClickable = true
        id = View.generateViewId()
        this.isChecked = isChecked
    }
}
