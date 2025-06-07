package com.rige.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.rige.databinding.FragmentCustomerFormBinding
import com.rige.models.Customer
import com.rige.viewmodels.CustomerViewModel
import java.util.UUID

class CustomerFormFragment : Fragment() {

    private lateinit var binding: FragmentCustomerFormBinding
    private val viewModel: CustomerViewModel by activityViewModels()

    private var customerId: String? = null
    private var currentCustomer: Customer? = null
    private var formInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCustomerFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        customerId = arguments?.getString("customerId")
        setupListeners()

        if (customerId != null) {
            loadCustomer(customerId!!)
        }
    }

    private fun setupListeners() {
        binding.btnSaveCustomer.setOnClickListener {
            handleSaveClick()
        }
    }

    private fun loadCustomer(id: String) {
        viewModel.getCustomerById(id).observe(viewLifecycleOwner) { customer ->
            customer?.let {
                currentCustomer = it
                if (!formInitialized) {
                    populateForm(it)
                    formInitialized = true
                }
            }
        }
    }

    private fun populateForm(customer: Customer) = with(binding) {
        etCustomerName.setText(customer.name)
        etCustomerPaternal.setText(customer.paternalSurname)
        etCustomerMaternal.setText(customer.maternalSurname)
        etCustomerPhone.setText(customer.phoneNumber)
        etCustomerAddress.setText(customer.address)
    }

    private fun handleSaveClick() = with(binding) {
        val name = etCustomerName.text.toString().trim()
        val paternal = etCustomerPaternal.text.toString().trim()
        val maternal = etCustomerMaternal.text.toString().trim()
        val phone = etCustomerPhone.text.toString().trim()
        val address = etCustomerAddress.text.toString().trim()

        if (name.isBlank() || paternal.isBlank()) {
            Toast.makeText(requireContext(), "Nombre y Apellido Paterno son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val newCustomer = (currentCustomer?.copy(
            name = name,
            paternalSurname = paternal,
            maternalSurname = maternal.ifBlank { null },
            phoneNumber = phone.ifBlank { null },
            address = address.ifBlank { null }
        ) ?: Customer(
            id = UUID.randomUUID().toString(),
            name = name,
            paternalSurname = paternal,
            maternalSurname = maternal.ifBlank { null },
            phoneNumber = phone.ifBlank { null },
            address = address.ifBlank { null }
        ))

        if (currentCustomer == newCustomer) {
            findNavController().popBackStack()
            return
        }

        if (currentCustomer == null) {
            viewModel.saveCustomer(newCustomer) {
                findNavController().popBackStack()
            }
        } else {
            viewModel.updateCustomer(newCustomer) {
                findNavController().popBackStack()
            }
        }
    }
}