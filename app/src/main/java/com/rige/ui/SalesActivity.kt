package com.rige.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.rige.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SalesActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private var showShareButton = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_sales) as NavHostFragment
        navController = navHostFragment.navController

        setupActionBarWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            // Mostrar botón solo en SaleDetailsFragment
            showShareButton = destination.id == R.id.saleDetailsFragment
            invalidateOptionsMenu()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (showShareButton) {
            menuInflater.inflate(R.menu.menu_sale_details, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_sales)
                val currentFragment = navHostFragment?.childFragmentManager?.fragments?.firstOrNull()
                if (currentFragment is SaleDetailsFragment) {
                    currentFragment.shareSaleSummary()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
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
}
