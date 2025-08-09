package com.rige.ui

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.rige.R

class AccessDeniedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access_denied)

        findViewById<Button>(R.id.btnRetry).setOnClickListener {
            finish() // Cierra esta pantalla y vuelve a MainActivity, que volverá a validar
        }
    }
}