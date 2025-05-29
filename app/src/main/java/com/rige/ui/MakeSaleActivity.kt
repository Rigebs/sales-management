package com.rige.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.activity.viewModels
import androidx.navigation.ui.setupActionBarWithNavController
import com.rige.R
import com.rige.viewmodels.CartViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MakeSaleActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private var cartItemCount = 0
    private var cartBadgeTextView: TextView? = null
    private val cartViewModel: CartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_sale)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_make_sale) as NavHostFragment
        navController = navHostFragment.navController

        setupActionBarWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        cartViewModel.cart.observe(this) { items ->
            cartItemCount = items.size
            updateCartBadge()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return if (!navController.navigateUp()) {
            finish()
            true
        } else {
            true
        }
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
}