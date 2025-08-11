package com.rige.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.rige.databinding.FragmentCategoryFormBinding
import com.rige.models.Category
import com.rige.viewmodels.CategoryViewModel
import java.util.UUID

class CategoryFormFragment : Fragment() {

    private var _binding: FragmentCategoryFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoryViewModel by activityViewModels()

    private var categoryId: String? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryId = arguments?.getString("categoryId")
        isEditMode = categoryId != null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isEditMode) viewModel.loadCategoryById(categoryId!!) else viewModel.clearSelectedCategory()

        observeViewModel()

        binding.btnSaveCategory.setOnClickListener {
            val name = binding.etCategoryName.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "El nombre no puede estar vacío",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (isEditMode) {
                val updatedCategory = Category(id = categoryId!!, name = name)
                viewModel.updateCategory(updatedCategory)
            } else {
                val newCategory = Category(id = UUID.randomUUID().toString(), name = name)
                viewModel.saveCategory(newCategory)
            }

            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewModel.selectedCategory.observe(viewLifecycleOwner) { category ->
            category?.let {
                binding.etCategoryName.setText(it.name)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}