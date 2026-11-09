package com.example.pr_52.ui

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pr_52.R
import com.example.pr_52.data.RateEntity
import com.example.pr_52.data.RatesRepository
import com.example.pr_52.databinding.ActivitySavedRatesBinding
import com.example.pr_52.databinding.DialogRateEditBinding
import com.example.pr_52.util.Formats
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/**
 * Экран сохранённых курсов: список из Room с добавлением, редактированием и удалением.
 */
class SavedRatesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedRatesBinding
    private lateinit var repository: RatesRepository
    private lateinit var adapter: RateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySavedRatesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        repository = RatesRepository(this)

        adapter = RateAdapter(
            onEdit = { showEditDialog(it) },
            onDelete = { confirmDelete(it) }
        )
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        binding.addFab.setOnClickListener { showAddDialog() }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        lifecycleScope.launch {
            val items = repository.all()
            adapter.submitList(items)
            binding.emptyText.isVisible = items.isEmpty()
            binding.recycler.isVisible = items.isNotEmpty()
        }
    }

    private fun showAddDialog() {
        val dialogBinding = DialogRateEditBinding.inflate(layoutInflater)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_add_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_save_short, null)
            .setNegativeButton(R.string.action_cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val pair = dialogBinding.pairInput.text?.toString()?.trim()?.uppercase().orEmpty()
                val rate = parseRate(dialogBinding.rateInput.text?.toString())
                val note = dialogBinding.noteInput.text?.toString()?.trim().orEmpty()

                dialogBinding.pairLayout.error = null
                dialogBinding.rateLayout.error = null

                if (!PAIR_REGEX.matches(pair)) {
                    dialogBinding.pairLayout.error = getString(R.string.error_pair)
                    return@setOnClickListener
                }
                if (rate == null) {
                    dialogBinding.rateLayout.error = getString(R.string.error_rate)
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    repository.add(
                        RateEntity(
                            pair = pair,
                            rate = rate,
                            note = note,
                            savedAt = System.currentTimeMillis()
                        )
                    )
                    dialog.dismiss()
                    refresh()
                }
            }
        }

        dialog.show()
    }

    private fun showEditDialog(item: RateEntity) {
        val dialogBinding = DialogRateEditBinding.inflate(layoutInflater)
        dialogBinding.pairInput.setText(item.pair)
        dialogBinding.rateInput.setText(Formats.rate(item.rate))
        dialogBinding.noteInput.setText(item.note)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_edit_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_save_short, null)
            .setNegativeButton(R.string.action_cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val pair = dialogBinding.pairInput.text?.toString()?.trim()?.uppercase().orEmpty()
                val rate = parseRate(dialogBinding.rateInput.text?.toString())
                val note = dialogBinding.noteInput.text?.toString()?.trim().orEmpty()

                dialogBinding.pairLayout.error = null
                dialogBinding.rateLayout.error = null

                if (!PAIR_REGEX.matches(pair)) {
                    dialogBinding.pairLayout.error = getString(R.string.error_pair)
                    return@setOnClickListener
                }
                if (rate == null) {
                    dialogBinding.rateLayout.error = getString(R.string.error_rate)
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    repository.update(item.copy(pair = pair, rate = rate, note = note))
                    dialog.dismiss()
                    refresh()
                }
            }
        }

        dialog.show()
    }

    private fun confirmDelete(item: RateEntity) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_title)
            .setMessage(getString(R.string.delete_message, item.pair))
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.delete_yes) { _, _ ->
                lifecycleScope.launch {
                    repository.delete(item)
                    Toast.makeText(
                        this@SavedRatesActivity,
                        R.string.deleted,
                        Toast.LENGTH_SHORT
                    ).show()
                    refresh()
                }
            }
            .show()
    }

    private fun parseRate(value: String?): Double? {
        val normalized = value?.trim()?.replace(',', '.') ?: return null
        val parsed = normalized.toDoubleOrNull() ?: return null
        return if (parsed > 0) parsed else null
    }

    companion object {
        private val PAIR_REGEX = Regex("[A-Z]{6}")
    }
}
