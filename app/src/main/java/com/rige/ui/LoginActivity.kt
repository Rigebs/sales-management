package com.rige.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.rige.MainActivity
import com.rige.clients.SupabaseClient
import com.rige.databinding.ActivityLoginBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            val session = SupabaseClient.client.auth.currentSessionOrNull()
            if (session != null) {
                goToMain()
            }
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email y contraseña requeridos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    SupabaseClient.client.auth.signInWith(Email) {
                        this.email = email
                        this.password = password
                    }
                    goToMain()
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            lifecycleScope.launch {
                try {
                    SupabaseClient.client.auth.signUpWith(Email) {
                        this.email = email
                        this.password = password
                    }
                    goToMain()
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun goToMain() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
