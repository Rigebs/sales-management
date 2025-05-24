package com.rige.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rige.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MakeSaleActivity : AppCompatActivity() {

    private var cartItemCount = 0
    private var cartBadgeTextView: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_sale)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_make_sale, menu)

        val menuItem = menu?.findItem(R.id.action_cart)

        var actionView = menuItem?.actionView
        if (actionView == null) {
            actionView = LayoutInflater.from(this).inflate(R.layout.menu_cart_layout, null)
            menuItem?.actionView = actionView
        }

        cartBadgeTextView = actionView?.findViewById<TextView>(R.id.cart_badge)

        updateCartBadge()

        actionView?.setOnClickListener {
            onOptionsItemSelected(menuItem!!)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cart -> {
                Toast.makeText(this, "Carrito presionado", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateCartBadge() {
        if (cartItemCount > 0) {
            cartBadgeTextView?.text = cartItemCount.toString()
            cartBadgeTextView?.visibility = View.VISIBLE
        } else {
            cartBadgeTextView?.visibility = View.GONE
        }
    }

    fun increaseCartCount() {
        cartItemCount++
        updateCartBadge()
        println("SE SUNMAA $cartItemCount")
    }
}