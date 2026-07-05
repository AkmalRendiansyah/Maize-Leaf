package com.akmal.maizeleaf.ui.viewArtikel

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.akmal.maizeleaf.R
import com.akmal.maizeleaf.databinding.ActivityPreviewArtikelBinding
import com.akmal.maizeleaf.helper.LoadingAnimator
import com.bumptech.glide.Glide

class PreviewArtikelActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPreviewArtikelBinding
    private lateinit var loadingAnimator: LoadingAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityPreviewArtikelBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val artikelId = intent.getIntExtra("ARTIKEL_ID", 0)
        val judul = intent.getStringExtra("ARTIKEL_JUDUL") ?: ""
        val deskripsi = intent.getStringExtra("ARTIKEL_DESKRIPSI") ?:""
        val referensi = intent.getStringExtra("ARTIKEL_REFERENSI") ?:""
        val created = intent.getStringExtra("ARTIKEL_CREATED") ?:""
        val gambar = intent.getStringExtra("ARTIKEL_GAMBAR") ?:""

        binding.tvTanggal.text = created
        binding.tvJudul.text = judul
        binding.tvDeskripsi.text = deskripsi
        binding.tvReferensi.text = referensi
        Glide.with(this)
            .load(gambar)
            .placeholder(R.drawable.image_not_found)
            .error(R.drawable.image_not_found)
            .into(binding.ivGambar)
        loadingAnimator = LoadingAnimator(binding.loadingRing, binding.loadingLogo)


    }
    private fun setLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE

        if (isLoading) {
            loadingAnimator.start()
        } else {
            loadingAnimator.stop()
        }
    }


}