package com.akmal.maizeleaf.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.akmal.maizeleaf.R
import com.akmal.maizeleaf.api.GetHistoryResponseItem
import com.akmal.maizeleaf.databinding.ItemHistoryBinding
import com.bumptech.glide.Glide

class HistoryAdapter(
    private val onHistoryClick: (GetHistoryResponseItem) -> Unit,
    private val onDeleteClick: (GetHistoryResponseItem) -> Unit
) : ListAdapter<GetHistoryResponseItem, HistoryAdapter.HistoryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GetHistoryResponseItem) {
            binding.namaPenyakit.text = item.penyakit
            binding.tvItemCreate.text = item.createdAt
            Glide.with(binding.photoUri.context)
                .load(item.gambar)  // URL gambar dari response
                .placeholder(R.drawable.logo_apk) // gambar sementara saat loading
                .error(R.drawable.logo_apk) // gambar saat error
                .into(binding.photoUri)
            binding.root.setOnClickListener {
                onHistoryClick(item)
            }
            binding.ivDelete.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }
    private fun truncateTo10Sentences(text: String?): String {
        if (text.isNullOrEmpty()) return ""
        val sentences = text.split(Regex("(?<=[.!?])\\s+"))
        return if (sentences.size <= 10) {
            text
        } else {
            sentences.take(10).joinToString(" ") + "..."
        }
    }
    companion object DiffCallback : DiffUtil.ItemCallback<GetHistoryResponseItem>() {
        override fun areItemsTheSame(oldItem: GetHistoryResponseItem, newItem: GetHistoryResponseItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GetHistoryResponseItem, newItem: GetHistoryResponseItem): Boolean {
            return oldItem == newItem
        }
    }
}
