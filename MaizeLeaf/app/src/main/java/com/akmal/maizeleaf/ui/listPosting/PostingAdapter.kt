package com.akmal.maizeleaf.ui.listPosting

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.akmal.maizeleaf.R
import com.akmal.maizeleaf.api.GetAllPostingResponseItem
import com.akmal.maizeleaf.databinding.ItemChatBinding

import com.akmal.maizeleaf.databinding.ItemHistoryBinding
import com.akmal.maizeleaf.ui.addPosting.CameraPostingActivity
import com.akmal.maizeleaf.ui.komentar.KomentarActivity
import com.bumptech.glide.Glide

class PostingAdapter(
    private val onPostingClick: (GetAllPostingResponseItem) -> Unit,
//    private val onDeleteClick: (GetHistoryResponseItem) -> Unit
) : ListAdapter<GetAllPostingResponseItem, PostingAdapter.PostingViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostingViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostingViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class PostingViewHolder(private val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GetAllPostingResponseItem) {
            binding.tvUsername.text = item.username
            binding.tvTime.text = item.createdAt
            binding.tvDescription.text = item.deskripsi
            binding.tvJumlahKomentar.text = item.jumlahKomentar.toString()
            Glide.with(binding.ivGambarPosting.context)
                .load(item.gambar)
                .placeholder(R.drawable.logo_apk)
                .error(R.drawable.logo_apk)
                .into(binding.ivGambarPosting)
            binding.root.setOnClickListener {
                onPostingClick(item)
            }
            binding.btnKomentar.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, KomentarActivity::class.java).apply {
                    putExtra("POSTING_ID", item.id)
                    putExtra("POSTING_USERNAME", item.username)
                    putExtra("POSTING_DESKRIPSI", item.deskripsi)
                    putExtra("POSTING_GAMBAR", item.gambar)
                }
                context.startActivity(intent)
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
    companion object DiffCallback : DiffUtil.ItemCallback<GetAllPostingResponseItem>() {
        override fun areItemsTheSame(oldItem: GetAllPostingResponseItem, newItem: GetAllPostingResponseItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GetAllPostingResponseItem, newItem: GetAllPostingResponseItem): Boolean {
            return oldItem == newItem
        }
    }
}
