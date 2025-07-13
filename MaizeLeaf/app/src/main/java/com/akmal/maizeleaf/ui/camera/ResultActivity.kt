package com.akmal.maizeleaf.ui.camera

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.akmal.maizeleaf.MainActivity
import com.akmal.maizeleaf.api.ApiConfig
import com.akmal.maizeleaf.data.UserPreference
import com.akmal.maizeleaf.data.dataStore
import com.akmal.maizeleaf.databinding.ActivityResultBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val resultText = intent.getStringExtra("resultText") ?: "No result"
        val imageUriString = intent.getStringExtra("imageUri")

        if (imageUriString.isNullOrEmpty()) {
            Log.e("ResultActivity", "imageUriString is null or empty")
            binding.resultText.text = "Gagal memuat gambar"
            return
        }

        val imageUri = Uri.parse(imageUriString)
        Log.d("ResultActivity", "Image URI: $imageUri")

        binding.resultText.text = resultText


        Glide.with(this)
            .load(imageUri)
            .into(binding.resultImage)


        getDeskripsiPenyakit(resultText, imageUri)

        binding.selesai.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun getDeskripsiPenyakit(nama: String, imageUri: Uri) {
        lifecycleScope.launch {
            try {
                val response = ApiConfig.getApiService().deskripsiPenyakit(nama)
                val deskripsi = response.deskripsi ?: "Deskripsi tidak tersedia"
                binding.isiDeskripsi.text = deskripsi


                uploadHistory(nama, deskripsi, imageUri)
            } catch (e: Exception) {
                Log.e("API_ERROR", "Gagal mengambil deskripsi: ${e.message}")
                binding.isiDeskripsi.text = "Terjadi kesalahan mengambil data."
            }
        }
    }

    private fun uploadHistory(penyakit: String, deskripsi: String, imageUri: Uri) {
        lifecycleScope.launch {
            try {
                val userPref = UserPreference.getInstance(applicationContext.dataStore)
                val user = userPref.getSession().first()
                val token = user.token

                if (token.isEmpty()) {
                    Log.e("POST_HISTORY", "Token tidak ditemukan")
                    return@launch
                }


                val inputStream = contentResolver.openInputStream(imageUri)
                val tempFile = File.createTempFile("upload", ".jpg", cacheDir)
                inputStream?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val requestImage = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("gambar", tempFile.name, requestImage)
                val penyakitPart = penyakit.toRequestBody("text/plain".toMediaType())

                val response = ApiConfig.getApiService().postHistory(
                    "Bearer $token",
                    penyakitPart,
                    imagePart
                )

                Log.d("POST_HISTORY", "Berhasil kirim riwayat: ${response.msg}")
            } catch (e: Exception) {
                Log.e("POST_HISTORY", "Gagal mengirim riwayat: ${e.message}")
            }
        }
    }
}
