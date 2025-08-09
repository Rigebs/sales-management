package com.rige

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.card.MaterialCardView
import com.jakewharton.threetenabp.AndroidThreeTen
import com.rige.clients.SupabaseClient
import com.rige.clients.SupabaseClient.waitForSupabaseSession
import com.rige.ui.AccessDeniedActivity
import com.rige.ui.CustomersActivity
import com.rige.ui.LoginActivity
import com.rige.ui.MakeSaleActivity
import com.rige.ui.OrdersActivity
import com.rige.ui.ProductsActivity
import com.rige.ui.SalesActivity
import com.rige.ui.PurchasesActivity
import com.rige.viewmodels.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        AndroidThreeTen.init(this)

        var keepSplash = true
        splashScreen.setKeepOnScreenCondition { keepSplash }

        MainScope().launch {
            val session = SupabaseClient.client.auth.sessionManager.loadSession()
            var user = SupabaseClient.client.auth.currentUserOrNull()

            println("Session: $session")
            println("User: $user")

            if (session == null || user == null) {
                val refreshedUser = waitForSupabaseSession()
                if (refreshedUser == null) {
                    SupabaseClient.client.auth.signOut()
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish()
                    return@launch
                }
                user = refreshedUser
            }

            val userId = user.id
            viewModel.validateAccess(userId)

            viewModel.hasAccess.collect { access ->
                if (access == null) return@collect

                if (!access) {
                    println("Acceso denegado")
                    startActivity(Intent(this@MainActivity, AccessDeniedActivity::class.java))
                } else {
                    keepSplash = false
                    setContentView(R.layout.activity_main)
                    setupCardListeners()
                }
            }
        }
    }

    private fun setupCardListeners() {
        findViewById<MaterialCardView>(R.id.cardGenerateSale).setOnClickListener {
            startActivity(Intent(this, MakeSaleActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardProducts).setOnClickListener {
            startActivity(Intent(this, ProductsActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardCustomers).setOnClickListener {
            startActivity(Intent(this, CustomersActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardSales).setOnClickListener {
            startActivity(Intent(this, SalesActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardRegisterOrder).setOnClickListener {
            startActivity(Intent(this, PurchasesActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardOrders).setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }
    }
}
