package com.akmal.maizeleaf.ui.artikel

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.akmal.maizeleaf.R
import com.akmal.maizeleaf.api.GetArtikelResponseItem
import com.akmal.maizeleaf.databinding.ItemArtikelBinding
import com.akmal.maizeleaf.ui.viewArtikel.PreviewArtikelActivity

import com.bumptech.glide.Glide

class ArtikelAdapter(
    private val onArtikelClick: (GetArtikelResponseItem) -> Unit,
//    private val onDeleteClick: (GetHistoryResponseItem) -> Unit
) : ListAdapter<GetArtikelResponseItem, ArtikelAdapter.ArtikelViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtikelViewHolder {
        val binding = ItemArtikelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtikelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtikelViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ArtikelViewHolder(private val binding: ItemArtikelBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GetArtikelResponseItem) {
            binding.tvJudul.text = item.judul
            binding.tvTanggal.text = item.createdAt
            binding.tvDeskripsi.text = item.deskripsi
            binding.tvReferensi.text = item.referensi
            Glide.with(binding.ivGambarArtikel.context)
                .load(item.gambar)
                .placeholder(R.drawable.logo_apk)
                .error(R.drawable.logo_apk)
                .into(binding.ivGambarArtikel)

            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, PreviewArtikelActivity::class.java).apply {
                    putExtra("ARTIKEL_ID", item.id)
                    putExtra("ARTIKEL_JUDUL", item.judul)
                    putExtra("ARTIKEL_CREATED", item.createdAt)
                    putExtra("ARTIKEL_DESKRIPSI", item.deskripsi)
                    putExtra("ARTIKEL_REFERENSI", item.referensi)
                    putExtra("ARTIKEL_GAMBAR", item.gambar)
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
    companion object DiffCallback : DiffUtil.ItemCallback<GetArtikelResponseItem>() {
        override fun areItemsTheSame(oldItem: GetArtikelResponseItem, newItem: GetArtikelResponseItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GetArtikelResponseItem, newItem: GetArtikelResponseItem): Boolean {
            return oldItem == newItem
        }
    }
}
