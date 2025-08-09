package com.rige.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.rige.MainActivity
import com.rige.R
import com.rige.clients.SupabaseClient
import com.rige.viewmodels.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AccessDeniedActivity : AppCompatActivity() {

    private val viewModel: ProfileViewModel by viewModels()

    private lateinit var btnRetry: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access_denied)

        btnRetry = findViewById(R.id.btnRetry)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.GONE

        btnRetry.setOnClickListener {
            retryAccessCheck()
        }

        // Observamos cambios en el flujo de acceso
        lifecycleScope.launchWhenStarted {
            viewModel.hasAccess.collectLatest { access ->
                if (access == null) return@collectLatest

                if (access) {
                    // Acceso válido: volver a MainActivity reiniciándola
                    val intent = Intent(this@AccessDeniedActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@AccessDeniedActivity, "Acceso denegado nuevamente", Toast.LENGTH_SHORT).show()
                    btnRetry.isEnabled = true
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun retryAccessCheck() {
        btnRetry.isEnabled = false
        progressBar.visibility = View.VISIBLE

        // Aquí obtén userId desde sesión o la forma que tengas
        val user = SupabaseClient.client.auth.currentUserOrNull()
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado. Redirigiendo al login.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        viewModel.validateAccess(user.id)
    }
}
