package com.akmal.maizeleaf.ui.camera

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.akmal.maizeleaf.databinding.ActivityUrlBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class UrlActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUrlBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUrlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(com.akmal.maizeleaf.R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnClassifyUrl.setOnClickListener {
            val url = binding.etImageUrl.text.toString().trim()

            if (url.isEmpty()) {
                binding.tvError.text = "URL tidak boleh kosong"
                binding.tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            binding.tvError.visibility = View.GONE
            binding.urlProgressBar.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    val isImage = withContext(Dispatchers.IO) {
                        val connection = URL(url).openConnection() as HttpURLConnection
                        connection.doInput = true
                        connection.connect()

                        val mimeType = connection.contentType
                        if (!mimeType.startsWith("image/")) {
                            throw Exception("URL bukan gambar. MIME type: $mimeType")
                        }
                        true
                    }

                    if (isImage) {
                        binding.urlProgressBar.visibility = View.GONE
                        val intent = Intent(this@UrlActivity, PreviewActivity::class.java).apply {
                            putExtra("isFromCamera", false)
                            putExtra("imageUrl", url)
                            putExtra("autoAnalyze", true)
                        }
                        startActivity(intent)
                    }

                } catch (e: Exception) {
                    binding.urlProgressBar.visibility = View.GONE
                    binding.tvError.text = "Gagal mengambil gambar: ${e.message}"
                    binding.tvError.visibility = View.VISIBLE
                    Toast.makeText(this@UrlActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
