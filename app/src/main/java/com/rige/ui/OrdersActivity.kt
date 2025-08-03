package com.rige.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.rige.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrdersActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_orders) as NavHostFragment
        navController = navHostFragment.navController

        setupActionBarWithNavController(navController)

        navController.addOnDestinationChangedListener { _, _, _ ->
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
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