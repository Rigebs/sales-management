package com.rige.ui

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.rige.R
import com.rige.databinding.FragmentDebtsBinding
import com.rige.models.Customer
import com.rige.viewmodels.CustomerViewModel
import java.math.BigDecimal

class DebtsFragment : Fragment() {

    private var _binding: FragmentDebtsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CustomerViewModel by activityViewModels()

    private var totalDebt: BigDecimal = BigDecimal.ZERO
    private var customerId: String? = null
    private var customer: Customer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDebtsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtenemos customerId desde argumentos
        customerId = arguments?.getString("customerId")
        if (customerId != null) {
            loadCustomer(customerId!!)
        }

        // Acción del botón
        binding.btnPay.setOnClickListener {
            showPaymentDialog()
        }
    }

    private fun loadCustomer(id: String) {
        viewModel.getCustomerById(id).observe(viewLifecycleOwner) { cust ->
            if (cust != null) {
                customer = cust
                binding.tvCustomerName.text = cust.name + " " + cust.paternalSurname + " " + (cust.maternalSurname ?: "")
                totalDebt = cust.totalDebt ?: BigDecimal.ZERO
                updateDebtText()
            } else {
                Toast.makeText(requireContext(), "Cliente no encontrado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateDebtText() {
        binding.tvTotalDebt.text = "S/. ${"%.2f".format(totalDebt)}"
        binding.btnPay.isEnabled = totalDebt > BigDecimal.ZERO
    }

    private fun showPaymentDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_payment, null)
        val input = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etAmount)

        // Agregar TextWatcher para validar el máximo
        input.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val entered = s.toString().toBigDecimalOrNull()
                if (entered != null && entered > totalDebt) {
                    input.error = "No puede ser mayor a S/. ${"%.2f".format(totalDebt)}"
                }
            }
        })

        AlertDialog.Builder(requireContext())
            .setTitle("Pagar deuda")
            .setMessage("Ingrese el monto que desea pagar")
            .setView(dialogView)
            .setPositiveButton("Pagar") { _, _ ->
                val amountText = input.text.toString()
                val amount = amountText.toBigDecimalOrNull()

                if (amount != null && amount > BigDecimal.ZERO) {
                    if (amount > totalDebt) {
                        Toast.makeText(requireContext(), "Monto mayor a la deuda", Toast.LENGTH_SHORT).show()
                    } else {
                        totalDebt = totalDebt.subtract(amount)
                        updateDebtText()

                        customer?.let { cust ->
                            val updatedCustomer = cust.copy(totalDebt = totalDebt)
                            viewModel.updateCustomer(updatedCustomer) {
                                Toast.makeText(requireContext(), "Pago registrado", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Monto inválido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}