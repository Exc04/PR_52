package com.example.pr_52.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pr_52.R
import com.example.pr_52.data.RateEntity
import com.example.pr_52.databinding.ItemRateBinding
import com.example.pr_52.util.Formats

class RateAdapter(
    private val onEdit: (RateEntity) -> Unit,
    private val onDelete: (RateEntity) -> Unit
) : ListAdapter<RateEntity, RateAdapter.RateViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RateViewHolder {
        val binding = ItemRateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RateViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RateViewHolder(
        private val binding: ItemRateBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RateEntity) {
            val quote = item.pair.drop(3)

            binding.pairText.text = item.pair
            binding.rateText.text = binding.root.context.getString(
                R.string.rate_item,
                Formats.rate(item.rate),
                quote
            )
            binding.noteText.isVisible = item.note.isNotBlank()
            binding.noteText.text = item.note
            binding.dateText.text = Formats.date(item.savedAt)

            binding.editButton.setOnClickListener { onEdit(item) }
            binding.deleteButton.setOnClickListener { onDelete(item) }
            binding.root.setOnClickListener { onEdit(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<RateEntity>() {

        override fun areItemsTheSame(oldItem: RateEntity, newItem: RateEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: RateEntity, newItem: RateEntity): Boolean =
            oldItem == newItem
    }
}
