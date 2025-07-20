package com.rige

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.card.MaterialCardView
import com.jakewharton.threetenabp.AndroidThreeTen
import com.rige.clients.SupabaseClient
import com.rige.clients.SupabaseClient.waitForSupabaseSession
import com.rige.ui.CustomersActivity
import com.rige.ui.LoginActivity
import com.rige.ui.MakeSaleActivity
import com.rige.ui.ProductsActivity
import com.rige.ui.SalesActivity
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        AndroidThreeTen.init(this)

        var keepSplash = true
        splashScreen.setKeepOnScreenCondition { keepSplash }

        MainScope().launch {
            var session = SupabaseClient.client.auth.sessionManager.loadSession()
            var user = SupabaseClient.client.auth.currentUserOrNull()

            if (session == null || user == null) {
                val refreshedUser = waitForSupabaseSession()
                if (refreshedUser == null) {
                    SupabaseClient.client.auth.signOut()
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish()
                    return@launch
                }

                session = SupabaseClient.client.auth.sessionManager.loadSession()
                user = refreshedUser
            }

            Log.d("SessionDebug", "Access Token: ${session?.accessToken}")
            Log.d("SessionDebug", "Refresh Token: ${session?.refreshToken}")

            keepSplash = false
            setContentView(R.layout.activity_main)
            setupCardListeners()
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
    }
}
