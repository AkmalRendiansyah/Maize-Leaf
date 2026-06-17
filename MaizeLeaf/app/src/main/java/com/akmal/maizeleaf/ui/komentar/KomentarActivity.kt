package com.akmal.maizeleaf.ui.komentar

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.akmal.maizeleaf.R
import com.akmal.maizeleaf.api.ApiConfig
import com.akmal.maizeleaf.data.UserPreference
import com.akmal.maizeleaf.data.dataStore
import com.akmal.maizeleaf.databinding.ActivityKomentarBinding
import com.akmal.maizeleaf.ui.login.LoginActivity
import com.bumptech.glide.Glide

class KomentarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKomentarBinding
    private lateinit var viewModel: KomentarViewModel
    private lateinit var komentarAdapter: KomentarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityKomentarBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val postingId = intent.getIntExtra("POSTING_ID", 0)
        val deskripsi = intent.getStringExtra("POSTING_DESKRIPSI") ?: ""
        val gambar = intent.getStringExtra("POSTING_GAMBAR")

        binding.tvDescription.text = deskripsi
        Glide.with(this)
            .load(gambar)
            .placeholder(R.drawable.image_not_found)
            .error(R.drawable.image_not_found)
            .into(binding.ivGambarPosting)

        val pref = UserPreference.getInstance(dataStore)
        val apiService = ApiConfig.getApiService()
        val factory = KomentarViewModelFactory(pref, apiService)
        viewModel = ViewModelProvider(this, factory)[KomentarViewModel::class.java]

        komentarAdapter = KomentarAdapter()
        binding.rvKomentar.apply {
            layoutManager = LinearLayoutManager(this@KomentarActivity)
            adapter = komentarAdapter
        }

        viewModel.getSession().observe(this) { user ->
            if (user.token.isNotEmpty()) {
                viewModel.getKomentar(user.token, postingId)
            }
        }

        viewModel.komentarList.observe(this) { list ->
            komentarAdapter.submitList(list)
        }

        binding.btnKirim.setOnClickListener {
            val teks = binding.etKomentar.text.toString().trim()
            if (teks.isNotEmpty()) {
                viewModel.getSession().observe(this) { user ->
                    viewModel.kirimKomentar(user.token, postingId, teks)
                    binding.etKomentar.setText("")
                }
            }
        }
        viewModel.isLoading.observe(this) { isLoading ->
            setLoading(isLoading)
        }
        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
        viewModel.komentarList.observe(this) { komentarList ->
            if (komentarList.isNullOrEmpty()) {
                showNoHistory()
            } else {
                binding.tvNoKomentar.visibility = View.GONE
                binding.rvKomentar.visibility = View.VISIBLE
                komentarAdapter.submitList(komentarList)
            }
        }

    }
    private fun showNoHistory() {
        binding.tvNoKomentar.visibility = View.VISIBLE
        binding.rvKomentar.visibility = View.GONE
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnKirim.isEnabled = !isLoading
    }

}