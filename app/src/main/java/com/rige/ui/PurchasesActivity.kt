package com.rige.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.rige.R
import com.rige.viewmodels.PurchaseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PurchasesActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private var purchaseCount = 0
    private var purchaseBadgeTextView: TextView? = null
    private val purchaseViewModel: PurchaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchases)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_purchases) as NavHostFragment
        navController = navHostFragment.navController

        setupActionBarWithNavController(navController)

        navController.addOnDestinationChangedListener { _, _, _ ->
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        purchaseViewModel.purchase.observe(this) { items ->
            purchaseCount = items.size
            updatePurchaseBadge()
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
        menuInflater.inflate(R.menu.menu_generate_purchase, menu)

        val menuItem = menu?.findItem(R.id.action_purchase)

        var actionView = menuItem?.actionView
        if (actionView == null) {
            actionView = LayoutInflater.from(this).inflate(
                R.layout.menu_purchase_layout,
                FrameLayout(this),
                false)
            menuItem?.actionView = actionView
        }

        purchaseBadgeTextView = actionView?.findViewById(R.id.purchase_badge)

        return true
    }

    private fun updatePurchaseBadge() {
        if (purchaseCount > 0) {
            purchaseBadgeTextView?.text = purchaseCount.toString()
            purchaseBadgeTextView?.visibility = View.VISIBLE
        } else {
            purchaseBadgeTextView?.visibility = View.GONE
        }
    }
}