package com.akmal.maizeleaf.ui.komentar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.akmal.maizeleaf.api.GetKomentarResponseItem
import com.akmal.maizeleaf.databinding.ItemKomentarBinding

class KomentarAdapter : ListAdapter<GetKomentarResponseItem, KomentarAdapter.KomentarViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KomentarViewHolder {
        val binding = ItemKomentarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KomentarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KomentarViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class KomentarViewHolder(private val binding: ItemKomentarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GetKomentarResponseItem) {
            binding.tvUsername.text = item.username
            binding.tvKomentar.text = item.komentar
            binding.tvWaktu.text = item.createdAt
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<GetKomentarResponseItem>() {
        override fun areItemsTheSame(
            oldItem: GetKomentarResponseItem,
            newItem: GetKomentarResponseItem
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: GetKomentarResponseItem,
            newItem: GetKomentarResponseItem
        ) = oldItem == newItem
    }
}