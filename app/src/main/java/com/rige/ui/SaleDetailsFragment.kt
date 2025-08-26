package com.rige.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.rige.adapters.SaleDetailAdapter
import com.rige.databinding.FragmentSaleDetailsBinding
import com.rige.utils.formatToReadable
import com.rige.viewmodels.SaleViewModel
import androidx.core.graphics.createBitmap
import java.io.File
import java.io.FileOutputStream

class SaleDetailsFragment : Fragment() {

    private val viewModel: SaleViewModel by activityViewModels()
    private lateinit var binding: FragmentSaleDetailsBinding
    private lateinit var adapter: SaleDetailAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSaleDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val saleId = arguments?.getString("saleId")

        adapter = SaleDetailAdapter()
        binding.recyclerSaleDetails.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSaleDetails.adapter = adapter

        saleId?.let {
            viewModel.getSaleWithDetailsById(it)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.saleWithDetails.observe(viewLifecycleOwner) { saleWithDetails ->
            if (!saleWithDetails.isNullOrEmpty()) {
                val saleHeader = saleWithDetails.first()
                binding.tvSaleDate.text = saleHeader.date.formatToReadable()
                binding.tvTotalAmount.text = "S/. %.2f".format(saleHeader.total)
                binding.tvStatus.text = if (saleHeader.isPaid) "Pagado" else "Pendiente"
                binding.tvCustomerName.text = saleHeader.customerName ?: "Varios"

                adapter.submitList(saleWithDetails)
            }
        }
    }

    fun shareSaleSummary() {
        val bitmap = getBitmapFromView(binding.layoutSaleRoot)

        val file = File(requireContext().cacheDir, "sale_summary.png")
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(shareIntent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "WhatsApp no instalado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        // Medir el tamaño completo del layout (incluido el contenido del RecyclerView)
        view.measure(
            View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        // Crear el bitmap
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }
}