package com.rige.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rige.databinding.FragmentOrderFiltersBottomSheetBinding
import org.threeten.bp.LocalDate

class OrderFiltersBottomSheetFragment(
    private val onFilterApplied: (LocalDate?, LocalDate?) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentOrderFiltersBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderFiltersBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Start Date Picker
        binding.startDateEditText.setOnClickListener {
            showDatePicker { date ->
                startDate = date
                binding.startDateEditText.setText(date.toString())
            }
        }

        // End Date Picker
        binding.endDateEditText.setOnClickListener {
            showDatePicker { date ->
                endDate = date
                binding.endDateEditText.setText(date.toString())
            }
        }

        // Apply Filter
        binding.btnApply.setOnClickListener {
            if (startDate != null && endDate != null && endDate!!.isBefore(startDate)) {
                Toast.makeText(requireContext(), "End date cannot be before start date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            onFilterApplied(startDate, endDate)
            dismiss()
        }


        // Clear Filter
        binding.btnClear.setOnClickListener {
            startDate = null
            endDate = null
            binding.startDateEditText.setText("")
            binding.endDateEditText.setText("")
            onFilterApplied(null, null)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDatePicker(onDateSelected: (LocalDate) -> Unit) {
        val now = LocalDate.now()
        val dialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                onDateSelected(selectedDate)
            },
            now.year,
            now.monthValue - 1,
            now.dayOfMonth
        )
        dialog.show()
    }
}
