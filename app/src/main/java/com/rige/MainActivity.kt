package com.rige

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.jakewharton.threetenabp.AndroidThreeTen
import com.rige.ui.CustomersActivity
import com.rige.ui.MakeSaleActivity
import com.rige.ui.ProductsActivity
import com.rige.ui.SalesActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_main)

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