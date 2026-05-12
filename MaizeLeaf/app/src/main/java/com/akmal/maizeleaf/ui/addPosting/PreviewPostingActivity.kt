package com.akmal.maizeleaf.ui.addPosting

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.akmal.maizeleaf.MainActivity
import com.akmal.maizeleaf.R
import com.akmal.maizeleaf.api.ApiConfig
import com.akmal.maizeleaf.data.UserPreference
import com.akmal.maizeleaf.data.dataStore
import com.akmal.maizeleaf.databinding.ActivityPreviewBinding
import com.akmal.maizeleaf.databinding.ActivityPreviewPostingBinding
import com.akmal.maizeleaf.helper.ImageClassifierHelper
import com.akmal.maizeleaf.ui.camera.CameraActivity
import com.akmal.maizeleaf.ui.camera.ResultActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.URL

class PreviewPostingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPreviewPostingBinding
    private var currentImageUri: Uri? = null
    private var isFromCamera: Boolean = false





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewPostingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        isFromCamera = intent.getBooleanExtra("isFromCamera", false)
        val imageUriString = intent.getStringExtra("imageUri")
        val imageUrlString = intent.getStringExtra("imageUrl")


        if (imageUriString.isNullOrEmpty()) {
            Log.e("ResultActivity", "imageUriString is null or empty")
            return
        }

        currentImageUri  = Uri.parse(imageUriString)

        Log.d("ResultActivity", "Image URI: $currentImageUri ")

        if (imageUriString != null) {
            binding.previewImageView.setImageURI(currentImageUri)
        } else if (imageUrlString != null) {
            Glide.with(this)
                .load(imageUrlString)
                .into(binding.previewImageView)
        }

        if (isFromCamera) {
            binding.previewImageView.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            binding.previewImageView.scaleType = ImageView.ScaleType.FIT_CENTER
        }



        setupAction()


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupAction() {

        binding.btnSave.setOnClickListener {

            val deskripsi = binding.etDescription.text.toString()
            if (validateInput(deskripsi)) {
                currentImageUri?.let { uri->
                    uploadChat(deskripsi,uri)
                } ?: Toast.makeText(this, "Gambar tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnBack.setOnClickListener {
            finish()
        }



    }
    private fun validateInput( deskripsi: String): Boolean {
        return when {
            deskripsi.isEmpty() -> {
                binding.etDescription.error = "deskripsi tidak boleh kosong"
                false
            }

            else -> true
        }
    }
    private fun navigateToMain(isSuccess: Boolean) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("UPLOAD_STATUS", isSuccess)
            putExtra("UPLOAD_MESSAGE", if (isSuccess) "Upload berhasil!" else "Upload gagal!")
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    private fun uploadChat(deskripsi: String, imageUri: Uri) {
        binding.btnSave.isEnabled = false
        binding.btnBack.isEnabled = false
        binding.etDescription.isEnabled = false

        lifecycleScope.launch {
            try {
                val userPref = UserPreference.getInstance(applicationContext.dataStore)
                val user = userPref.getSession().first()
                val token = user.token

                if (token.isEmpty()) {
                    Log.e("POST_CHAT", "Token tidak ditemukan")
                    withContext(Dispatchers.Main) {
                        binding.btnSave.isEnabled = true
                        binding.btnBack.isEnabled = true
                        binding.etDescription.isEnabled = true
                        Toast.makeText(this@PreviewPostingActivity, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
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
                val deskripsi = deskripsi.toRequestBody("text/plain".toMediaType())

                val response = ApiConfig.getApiService().postChat(
                    "Bearer $token",
                    deskripsi,
                    imagePart
                )

                Log.d("POST_CHAT", "Berhasil kirim riwayat: ${response.msg}")
                withContext(Dispatchers.Main) {
                    navigateToMain(isSuccess = true)
                }
            } catch (e: Exception) {
                Log.e("POST_CHAT", "Gagal mengirim riwayat: ${e.message}")
                withContext(Dispatchers.Main) {
                    binding.btnSave.isEnabled = true
                    binding.btnBack.isEnabled = true
                    binding.etDescription.isEnabled=true
                    navigateToMain(isSuccess = false)
                }
            }
        }
    }

}