package com.example.pr_52.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.pr_52.R
import com.example.pr_52.api.RatesApi
import com.example.pr_52.data.LastRateCache
import com.example.pr_52.data.Prefs
import com.example.pr_52.data.RateEntity
import com.example.pr_52.data.RatesRepository
import com.example.pr_52.databinding.ActivityMainBinding
import com.example.pr_52.util.Formats
import kotlinx.coroutines.launch

/**
 * Главный экран: запрос курса через API и сохранение результата в Room.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var api: RatesApi
    private lateinit var repository: RatesRepository

    private var lastPair: String? = null
    private var lastRate: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val user = Prefs.loadUser(this)
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding.greeting.text = getString(R.string.greeting, user.name)

        api = RatesApi(this)
        repository = RatesRepository(this)

        binding.pairInput.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, RatesApi.POPULAR_PAIRS)
        )
        if (binding.pairInput.text.isNullOrBlank()) {
            binding.pairInput.setText(RatesApi.POPULAR_PAIRS.first())
        }

        binding.fetchButton.setOnClickListener { requestRate() }
        binding.saveButton.setOnClickListener { saveRate() }
        binding.savedButton.setOnClickListener {
            startActivity(Intent(this, SavedRatesActivity::class.java))
        }

        // Последний ответ API хранится в SharedPreferences в формате JSON
        Prefs.loadLastRate(this)?.takeIf { it.pair.isNotBlank() }?.let { cached ->
            binding.pairInput.setText(cached.pair)
            lastPair = cached.pair
            lastRate = cached.rate
            showResult(cached.pair, cached.rate, cached.rawJson, cached.source)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_logout -> {
            Prefs.clearUser(this)
            startActivity(
                Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private fun requestRate() {
        val pair = binding.pairInput.text?.toString()?.trim()?.uppercase().orEmpty()

        if (!PAIR_REGEX.matches(pair)) {
            binding.pairLayout.error = getString(R.string.error_pair_format)
            return
        }
        binding.pairLayout.error = null

        setLoading(true)

        api.fetchRate(
            pair = pair,
            onSuccess = { result ->
                setLoading(false)
                lastPair = result.pair
                lastRate = result.rate
                Prefs.saveLastRate(
                    this,
                    LastRateCache(
                        pair = result.pair,
                        rate = result.rate,
                        rawJson = result.rawJson,
                        savedAt = System.currentTimeMillis(),
                        source = result.source
                    )
                )
                showResult(result.pair, result.rate, result.rawJson, result.source)
            },
            onError = { message ->
                setLoading(false)
                Toast.makeText(
                    this,
                    getString(R.string.error_api, message),
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.isVisible = loading
        binding.fetchButton.isEnabled = !loading
        binding.fetchButton.text =
            getString(if (loading) R.string.loading else R.string.action_fetch)
    }

    private fun showResult(pair: String, rate: Double, rawJson: String, source: String) {
        val base = pair.take(3)
        val quote = pair.drop(3)

        binding.pairResult.text = pair
        binding.rateResult.text = getString(R.string.rate_template, base, Formats.rate(rate), quote)
        binding.sourceResult.text = getString(R.string.rate_source, source)
        binding.jsonResult.text = rawJson
        binding.resultCard.isVisible = true
        binding.saveButton.isEnabled = true
    }

    private fun saveRate() {
        val pair = lastPair ?: return
        val rate = lastRate ?: return

        lifecycleScope.launch {
            repository.add(RateEntity(pair = pair, rate = rate))
            Toast.makeText(this@MainActivity, R.string.saved_success, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private val PAIR_REGEX = Regex("[A-Z]{6}")
    }
}
