package com.rige.ui

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.rige.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SalesActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_sales) as NavHostFragment
        navController = navHostFragment.navController

        setupActionBarWithNavController(navController)

        navController.addOnDestinationChangedListener { _, _, _ ->
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        supportActionBar?.apply {
            val progress = ProgressBar(this@SalesActivity).apply {
                isIndeterminate = true
                visibility = View.GONE
                val size = resources.getDimensionPixelSize(R.dimen.actionbar_progress_size)
                layoutParams = ActionBar.LayoutParams(size, size).apply {
                    gravity = Gravity.END or Gravity.CENTER_VERTICAL
                }
            }

            progressBar = progress

            setDisplayShowCustomEnabled(true)
            setCustomView(progress, ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
            })
        }
    }

    fun showProgressBarInActionBar(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
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