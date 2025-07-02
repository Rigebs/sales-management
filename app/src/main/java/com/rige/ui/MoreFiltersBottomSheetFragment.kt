package com.rige.ui

import android.app.DatePickerDialog
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.rige.FilterCallback
import com.rige.R
import com.rige.models.extra.FilterOptions
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class MoreFiltersBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var etDateFrom: EditText
    private lateinit var etDateTo: EditText
    private lateinit var etAmountMin: EditText
    private lateinit var etAmountMax: EditText

    private var selectedDateFrom: LocalDate? = null
    private var selectedDateTo: LocalDate? = null

    var filterCallback: FilterCallback? = null

    private var existingFilters: FilterOptions? = null

    fun setInitialFilters(filters: FilterOptions) {
        existingFilters = filters
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_more_filters_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        etDateFrom = view.findViewById(R.id.etDateFrom)
        etDateTo = view.findViewById(R.id.etDateTo)
        etAmountMin = view.findViewById(R.id.etAmountMin)
        etAmountMax = view.findViewById(R.id.etAmountMax)
        setupDatePickers()

        existingFilters?.let { populateFields(it) }

        view.findViewById<Button>(R.id.btnApplyFilters).setOnClickListener {
            val filters = FilterOptions(
                dateFrom = selectedDateFrom,
                dateTo = selectedDateTo,
                amountMin = etAmountMin.text.toString().toDoubleOrNull(),
                amountMax = etAmountMax.text.toString().toDoubleOrNull()
            )
            filterCallback?.onFiltersApplied(filters)
            dismiss()
        }

        view.findViewById<Button>(R.id.btnClearFilters)?.setOnClickListener {
            filterCallback?.onFiltersApplied(FilterOptions()) // filtros vacíos
            dismiss()
        }
    }

    private fun populateFields(filters: FilterOptions) {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        filters.dateFrom?.let {
            selectedDateFrom = it
            etDateFrom.setText(it.format(formatter))
        }

        filters.dateTo?.let {
            selectedDateTo = it
            etDateTo.setText(it.format(formatter))
        }

        filters.amountMin?.let {
            etAmountMin.setText(it.toString())
        }

        filters.amountMax?.let {
            etAmountMax.setText(it.toString())
        }
    }

    private fun setupDatePickers() {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        etDateFrom.setOnClickListener {
            showDatePicker { date ->
                selectedDateFrom = date
                etDateFrom.setText(date.format(formatter))
            }
        }

        etDateTo.setOnClickListener {
            showDatePicker { date ->
                selectedDateTo = date
                etDateTo.setText(date.format(formatter))
            }
        }
    }

    private fun showDatePicker(onDateSelected: (LocalDate) -> Unit) {
        val today = LocalDate.now()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                onDateSelected(LocalDate.of(year, month + 1, day))
            },
            today.year, today.monthValue - 1, today.dayOfMonth
        )
        datePicker.show()
    }
}