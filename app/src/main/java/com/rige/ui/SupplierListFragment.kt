package com.rige.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rige.R
import com.rige.adapters.SupplierListAdapter
import com.rige.databinding.FragmentSupplierListBinding
import com.rige.viewmodels.SupplierViewModel

class SupplierListFragment : Fragment() {

    private var _binding: FragmentSupplierListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SupplierViewModel by activityViewModels()

    private lateinit var adapter: SupplierListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSupplierListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        viewModel.loadSuppliers()
        setupUIActions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        adapter = SupplierListAdapter { supplier ->
            Toast.makeText(requireContext(), "Seleccionado: ${supplier.name}", Toast.LENGTH_SHORT).show()
            val action = SupplierListFragmentDirections.actionToEditSupplier(supplier.id)
            findNavController().navigate(action)
        }
        binding.rvSuppliers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSuppliers.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.suppliers.observe(viewLifecycleOwner) { suppliers ->
            adapter.submitList(suppliers)
            binding.rvSuppliers.visibility =
                if (!suppliers.isNullOrEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            if (loading) binding.rvSuppliers.visibility = View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupUIActions() {
        binding.searchViewSuppliers.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = viewModel.suppliers.value?.filter {
                    it.name.contains(newText ?: "", ignoreCase = true) ||
                            it.phone.contains(newText ?: "", ignoreCase = true)
                }
                adapter.submitList(filtered)
                return true
            }
        })

        binding.fabAddSupplier.setOnClickListener {
            findNavController().navigate(R.id.actionToEditSupplier)
        }
    }
}
