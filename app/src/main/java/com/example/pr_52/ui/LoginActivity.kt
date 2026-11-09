package com.example.pr_52.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.pr_52.R
import com.example.pr_52.data.Prefs
import com.example.pr_52.data.UserProfile
import com.example.pr_52.databinding.ActivityLoginBinding

/**
 * Приветственный экран с авторизацией.
 * Проверяет поля на пустоту и сохраняет пользователя в SharedPreferences (JSON).
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Prefs.loadUser(this)?.let { saved ->
            binding.nameInput.setText(saved.name)
            binding.passwordInput.setText(saved.password)
            binding.savedHint.text = getString(R.string.login_saved_hint, saved.name)
            binding.savedHint.isVisible = true
        }

        binding.loginButton.setOnClickListener { login() }
        binding.passwordInput.setOnEditorActionListener { _, _, _ ->
            login()
            true
        }
    }

    private fun login() {
        val name = binding.nameInput.text?.toString()?.trim().orEmpty()
        val password = binding.passwordInput.text?.toString().orEmpty()

        binding.nameLayout.error = null
        binding.passwordLayout.error = null

        var valid = true

        if (name.isEmpty()) {
            binding.nameLayout.error = getString(R.string.error_name_empty)
            valid = false
        }

        when {
            password.isEmpty() -> {
                binding.passwordLayout.error = getString(R.string.error_password_empty)
                valid = false
            }

            password.length < 4 -> {
                binding.passwordLayout.error = getString(R.string.error_password_short)
                valid = false
            }
        }

        if (!valid) return

        Prefs.saveUser(
            this,
            UserProfile(
                name = name,
                password = password,
                loginAt = System.currentTimeMillis()
            )
        )

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
