package com.akmal.maizeleaf.ui.camera

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.akmal.maizeleaf.R
import  com.akmal.maizeleaf.helper.ImageClassifierHelper
import com.akmal.maizeleaf.databinding.ActivityPreviewBinding

class PreviewActivity : AppCompatActivity() {


    private lateinit var binding: ActivityPreviewBinding
    private var currentImageUri: Uri? = null
    private var isFromCamera: Boolean = false




    private val openGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                openPreviewActivity(it, false)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        val imageUriString = intent.getStringExtra("imageUri")
        val imageUri = Uri.parse(imageUriString)
        currentImageUri = imageUri
        isFromCamera = intent.getBooleanExtra("isFromCamera", false)

        imageUri?.let {
            binding.previewImageView.setImageURI(it)

            if (isFromCamera) {
                binding.previewImageView.scaleType = ImageView.ScaleType.CENTER_CROP
            } else {
                binding.previewImageView.scaleType = ImageView.ScaleType.FIT_CENTER
            }
        }

        binding.buttonRetake.setOnClickListener {
            handleRetake()
        }
        binding.buttonTakePicture.setOnClickListener {
            currentImageUri?.let {
                analyzeImage(it)
            } ?: run {
                Toast.makeText(this, "Gambar belum tersedia", Toast.LENGTH_SHORT).show()
            }
        }

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }



    private fun handleRetake() {
        if (isFromCamera) {
            val cameraIntent = Intent(this, CameraActivity::class.java)
            startActivity(cameraIntent)
            finish()
        } else {
            openGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }
    private fun openPreviewActivity(uri: Uri, isFromCamera: Boolean) {
        val intent = Intent(this, PreviewActivity::class.java)
        intent.putExtra("isFromCamera", isFromCamera)
        intent.putExtra("imageUri", uri.toString())
        startActivity(intent)
        finish()
    }
    private fun analyzeImage(uri: Uri) {

        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra(ResultActivity.EXTRA_IMAGE_URI, currentImageUri.toString())
        startActivity(intent)
    }

}