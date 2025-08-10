package com.rige.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.rige.R
import com.rige.models.extra.CartItem
import java.math.BigDecimal

class CartAdapter(
    private val onQuantityChange: (CartItem, BigDecimal) -> Unit,
    private val onDelete: (CartItem) -> Unit
) : ListAdapter<CartItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_INTEGER = 0
        private const val TYPE_DECIMAL = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isDecimal) TYPE_DECIMAL else TYPE_INTEGER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_DECIMAL) {
            val view = layoutInflater.inflate(R.layout.item_cart_decimal, parent, false)
            DecimalViewHolder(view)
        } else {
            val view = layoutInflater.inflate(R.layout.item_cart, parent, false)
            IntegerViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is DecimalViewHolder) holder.bind(item)
        if (holder is IntegerViewHolder) holder.bind(item)
    }

    inner class IntegerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imgProduct = view.findViewById<ImageView>(R.id.imgProduct)
        private val txtName = view.findViewById<TextView>(R.id.txtName)
        private val txtPrice = view.findViewById<TextView>(R.id.txtPrice)
        private val txtStock = view.findViewById<TextView>(R.id.txtStock)
        private val txtSubtotal = view.findViewById<TextView>(R.id.txtSubtotal)
        private val txtQuantity = view.findViewById<TextView>(R.id.txtQuantity)
        private val btnIncrease = view.findViewById<ImageButton>(R.id.btnIncrease)
        private val btnDecrease = view.findViewById<ImageButton>(R.id.btnDecrease)
        private val btnDelete = view.findViewById<ImageButton>(R.id.btnDelete)

        fun bind(item: CartItem) {
            txtName.text = item.name
            txtPrice.text = "Precio: s/. ${"%.2f".format(item.price)}"

            txtStock.visibility = if (item.manageStock) View.VISIBLE else View.GONE

            txtStock.text = "Stock: ${item.stock}"
            txtSubtotal.text = "Subtotal: s/. ${"%.2f".format(item.price * item.count)}"
            txtQuantity.text = item.count.stripTrailingZeros().toPlainString()

            Glide.with(itemView.context).load(item.imageUrl).placeholder(R.drawable.ic_broken_image).into(imgProduct)

            btnIncrease.setOnClickListener { onQuantityChange(item, item.count + BigDecimal.ONE) }
            btnDecrease.setOnClickListener {
                val newCount = item.count - BigDecimal.ONE
                if (newCount > BigDecimal.ZERO) onQuantityChange(item, newCount) else onDelete(item)
            }
            btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    inner class DecimalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imgProduct = view.findViewById<ImageView>(R.id.imgProduct)
        private val txtName = view.findViewById<TextView>(R.id.txtName)
        private val txtPrice = view.findViewById<TextView>(R.id.txtPrice)
        private val txtStock = view.findViewById<TextView>(R.id.txtStock)
        private val txtSubtotal = view.findViewById<TextView>(R.id.txtSubtotal)
        private val txtQuantity = view.findViewById<TextView>(R.id.txtQuantityDecimal)
        private val btnEdit = view.findViewById<ImageButton>(R.id.btnEditDecimal)
        private val btnDelete = view.findViewById<ImageButton>(R.id.btnDeleteDecimal)

        fun bind(item: CartItem) {
            txtName.text = item.name
            txtPrice.text = "Precio (${item.measureUnit}): s/. ${"%.2f".format(item.price)}"

            txtStock.visibility = if (item.manageStock) View.VISIBLE else View.GONE

            txtStock.text = "Stock: ${item.stock} kg"
            txtSubtotal.text = "Subtotal: s/. ${"%.2f".format(item.price * item.count)}"

            txtQuantity.text = "${item.count.stripTrailingZeros()} kg"

            Glide.with(itemView.context).load(item.imageUrl).placeholder(R.drawable.ic_broken_image).into(imgProduct)

            btnEdit.setOnClickListener {
                showEditDecimalDialog(itemView.context, item)
            }

            btnDelete.setOnClickListener {
                onDelete(item)
            }
        }

        private fun showEditDecimalDialog(context: Context, item: CartItem) {
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_decimal, null)

            val editText = view.findViewById<TextInputEditText>(R.id.etQuantity)
            val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupSuggestions)

            editText.setText(item.count.stripTrailingZeros().toPlainString())

            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as Chip
                chip.setOnClickListener {
                    val rawValue = chip.text.toString().replace(" kg", "").trim()
                    val value = when (rawValue) {
                        "1/4" -> "0.25"
                        "1/2" -> "0.5"
                        "3/4" -> "0.75"
                        else -> rawValue // Asume que es un número decimal como "1"
                    }
                    editText.setText(value)
                }
            }

            AlertDialog.Builder(context)
                .setTitle("Editar cantidad")
                .setView(view)
                .setPositiveButton("Aceptar") { _, _ ->
                    val input = editText.text.toString().toBigDecimalOrNull()
                    if (input != null && input > BigDecimal.ZERO) {
                        onQuantityChange(item, input)
                    } else {
                        Toast.makeText(context, "Ingrese una cantidad válida", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem) = oldItem.productId == newItem.productId
        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem) = oldItem == newItem
    }
}