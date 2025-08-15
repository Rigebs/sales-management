package com.rige.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.rige.databinding.FragmentSupplierFormBinding
import com.rige.models.Supplier
import com.rige.viewmodels.SupplierViewModel
import org.threeten.bp.LocalDateTime
import java.util.UUID

class SupplierFormFragment : Fragment() {

    private var _binding: FragmentSupplierFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SupplierViewModel by activityViewModels()

    private var supplierId: String? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supplierId = arguments?.getString("supplierId")
        isEditMode = supplierId != null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSupplierFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isEditMode) {
            viewModel.loadSupplierById(supplierId!!)
        } else {
            viewModel.clearSelectedSupplier()
        }

        observeViewModel()

        binding.btnSaveSupplier.setOnClickListener {
            val name = binding.etSupplierName.text.toString().trim()
            val phone = binding.etSupplierPhone.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (phone.isEmpty()) {
                Toast.makeText(requireContext(), "El teléfono no puede estar vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isEditMode) {
                val updatedSupplier = Supplier(id = supplierId!!, name = name, phone = phone)
                viewModel.updateSupplier(updatedSupplier)
            } else {
                val newSupplier = Supplier(id = UUID.randomUUID().toString(), name = name, phone = phone)
                viewModel.saveSupplier(newSupplier)
            }

            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewModel.selectedSupplier.observe(viewLifecycleOwner) { supplier ->
            supplier?.let {
                binding.etSupplierName.setText(it.name)
                binding.etSupplierPhone.setText(it.phone)
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
